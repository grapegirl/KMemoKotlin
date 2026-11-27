package momo.kikiplus.com.kbucket;
import android.graphics.*;
import android.media.*;
import java.io.*;
import java.nio.*;
import java.util.*;
/** Silent portrait slides for Android sharing, encoded with the device AVC encoder. */
final class Movie {
 static final int W=544,H=960,FPS=12,FRAMES_PER_PHOTO=22;
 static int color(MediaCodecInfo.CodecCapabilities caps){
  for(int candidate:new int[]{MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar,MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar})
   for(int supported:caps.colorFormats)if(candidate==supported)return candidate;
  return 0;
 }
 static int clamp(int v){return Math.max(0,Math.min(255,v));}
 static byte[] frame(File f,boolean interleaved)throws Exception{
  Bitmap image=BitmapFactory.decodeFile(f.toString());if(image==null)throw new IOException("image");
  Bitmap bmp=Bitmap.createScaledBitmap(image,W,H,true);int[] rgb=new int[W*H];bmp.getPixels(rgb,0,W,0,0,W,H);if(bmp!=image)bmp.recycle();image.recycle();
  int pixels=W*H;byte[] yuv=new byte[pixels*3/2];
  for(int y=0;y<H;y++)for(int x=0;x<W;x++){
   int pos=y*W+x,v=rgb[pos],r=(v>>16)&255,g=(v>>8)&255,b=v&255;
   yuv[pos]=(byte)clamp(((66*r+129*g+25*b+128)>>8)+16);
   if(((x|y)&1)==0){int q=(y/2)*(W/2)+x/2;
    byte u=(byte)clamp(((-38*r-74*g+112*b+128)>>8)+128),cr=(byte)clamp(((112*r-94*g-18*b+128)>>8)+128);
    if(interleaved){yuv[pixels+2*q]=u;yuv[pixels+2*q+1]=cr;}else{yuv[pixels+q]=u;yuv[pixels+pixels/4+q]=cr;}
   }
  }return yuv;
 }
 static void encode(File dir,File file)throws Exception{
  File[] frames=dir.listFiles((d,n)->n.endsWith(".jpg"));if(frames==null||frames.length<2)throw new IOException("frames");Arrays.sort(frames);
  MediaCodec codec=null;int format=0;
  for(MediaCodecInfo info:new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos()){
   if(!info.isEncoder())continue;for(String t:info.getSupportedTypes())if("video/avc".equalsIgnoreCase(t)){
    try{int col=color(info.getCapabilitiesForType(t));if(col==0)continue;
     MediaFormat fm=MediaFormat.createVideoFormat(t,W,H);fm.setInteger(MediaFormat.KEY_COLOR_FORMAT,col);fm.setInteger(MediaFormat.KEY_BIT_RATE,1400000);fm.setInteger(MediaFormat.KEY_FRAME_RATE,FPS);fm.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,1);
     MediaCodec test=MediaCodec.createByCodecName(info.getName());try{test.configure(fm,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);test.start();codec=test;format=col;}catch(Exception e){test.release();} 
    }catch(Exception ignored){}
    if(codec!=null)break;
   }if(codec!=null)break;
  }
  if(codec==null)throw new IOException("no AVC encoder");MediaMuxer mux=null;boolean started=false;
  try{mux=new MediaMuxer(file.getPath(),MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);MediaCodec.BufferInfo bi=new MediaCodec.BufferInfo();int track=-1,at=0,last=-1,count=frames.length*FRAMES_PER_PHOTO;byte[] yuv=null;boolean ended=false,finished=false;long activity=System.currentTimeMillis();
   while(!finished){if(Thread.interrupted()||System.currentTimeMillis()-activity>45000)throw new IOException("encoder timeout");
    if(!ended){int slot=codec.dequeueInputBuffer(5000);if(slot>=0){if(at==count){codec.queueInputBuffer(slot,0,0,at*1000000L/FPS,MediaCodec.BUFFER_FLAG_END_OF_STREAM);ended=true;}
      else{int n=at/FRAMES_PER_PHOTO;if(last!=n){yuv=frame(frames[n],format==MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);last=n;}
       ByteBuffer in=codec.getInputBuffer(slot);in.clear();in.put(yuv);codec.queueInputBuffer(slot,0,yuv.length,at*1000000L/FPS,0);at++;}
      activity=System.currentTimeMillis();}}
    int out=codec.dequeueOutputBuffer(bi,5000);if(out==MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){track=mux.addTrack(codec.getOutputFormat());mux.start();started=true;}
    else if(out>=0){ByteBuffer buf=codec.getOutputBuffer(out);if((bi.flags&MediaCodec.BUFFER_FLAG_CODEC_CONFIG)==0&&bi.size>0&&started){buf.position(bi.offset);buf.limit(bi.offset+bi.size);mux.writeSampleData(track,buf,bi);}
     finished=(bi.flags&MediaCodec.BUFFER_FLAG_END_OF_STREAM)!=0;codec.releaseOutputBuffer(out,false);activity=System.currentTimeMillis();}
   }
  }finally{try{codec.stop();}finally{codec.release();}if(mux!=null){try{if(started)mux.stop();}finally{mux.release();}}}
 }
}
