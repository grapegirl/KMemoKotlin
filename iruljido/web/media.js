import fallback from '../assets/sun.jpg';
import {appName,blobPut,source,id,notice,cover,progress,stamp,allPhotos,get,mediaKeys,T,restoreData,normalize,displayDate,validMemo,all} from './store';
import {PDFDocument} from 'pdf-lib';
import {Zip,ZipPassThrough,Unzip,UnzipInflate,strToU8,strFromU8} from 'fflate';
export function imageLoad(src){return new Promise((ok,no)=>{let i=new Image();i.onload=()=>ok(i);i.onerror=()=>no(new Error('사진 파일을 열 수 없어요.'));i.src=src;});}
export const readURL=b=>new Promise((ok,no)=>{let r=new FileReader();r.onload=()=>ok(r.result);r.onerror=no;r.readAsDataURL(b)});
export const readBytes=b=>new Promise((ok,no)=>{let r=new FileReader();r.onload=()=>ok(new Uint8Array(r.result));r.onerror=no;r.readAsArrayBuffer(b)});
export const canvasBlob=(c,q=.87)=>new Promise((ok,no)=>c.toBlob(b=>b?ok(b):no(new Error('이미지를 만들 수 없어요.')),'image/jpeg',q));
export function canvas(w,h){let c=document.createElement('canvas');c.width=w;c.height=h;return c;}
export function drawCover(g,i,x,y,w,h,zoom=1,cx=.5,cy=.5){let s=Math.max(w/i.width,h/i.height)*zoom,sw=w/s,sh=h/s;g.drawImage(i,Math.max(0,Math.min(i.width-sw,i.width*cx-sw/2)),Math.max(0,Math.min(i.height-sh,i.height*cy-sh/2)),sw,sh,x,y,w,h);}
export async function imageSource(ref){let url=await source(ref);return imageLoad(url==='sun.jpg'?fallback:url);}
export async function imageBlob(file,max=1400,q=.85){let url=URL.createObjectURL(file);try{let i=await imageLoad(url),s=Math.min(1,max/Math.max(i.width,i.height)),c=canvas(Math.max(1,Math.round(i.width*s)),Math.max(1,Math.round(i.height*s)));let g=c.getContext('2d');g.fillStyle='#fff';g.fillRect(0,0,c.width,c.height);g.drawImage(i,0,0,c.width,c.height);return await canvasBlob(c,q);}finally{URL.revokeObjectURL(url);}}
export async function savePhotos(files){let a=[];for(let f of files)a.push(await blobPut(await imageBlob(f)));return a;}
export async function saveVideo(file){if(file.size>32*1024*1024)throw new Error('32MB 이하의 짧은 영상을 선택해 주세요.');return blobPut(file);}
export async function thumbnail(ref){let i=await imageSource(ref),s=Math.min(1,400/Math.max(i.width,i.height));let c=canvas(Math.round(i.width*s),Math.round(i.height*s));c.getContext('2d').drawImage(i,0,0,c.width,c.height);return c.toDataURL('image/jpeg',.65);}
export const bytesFrom64=s=>Uint8Array.from(atob(s),x=>x.charCodeAt(0));
export function textLines(g,t,w){let lines=[],line='';for(let ch of String(t||'')){if(ch==='\n'||(line&&g.measureText(line+ch).width>w)){lines.push(line);line='';}if(ch!=='\n')line+=ch;}if(line)lines.push(line);return lines;}
function boundedText(g,t,x,y,w,max,step){const lines=textLines(g,t,w);lines.slice(0,max).forEach((l,j)=>g.fillText(l+(j===max-1&&lines.length>max?'…':''),x,y+step*j));return y+Math.min(max,lines.length)*step;}
export const cardDefaults={design:'엽서',ratio:'정사각형',date:true,rate:true,note:true,zoom:1,cx:.5,cy:.5,masks:[]};
export async function drawCard(d,opt={},chosen=[]){
 let o={...cardDefaults,...opt},w=900,h=o.ratio==='스토리'?1600:o.ratio==='가로'?660:900,c=canvas(w,h),g=c.getContext('2d');
 let dark=o.design==='필름',plain=o.design==='기록지',ink=dark?'#fcf4e4':'#234b34',pad=52;
 g.fillStyle=dark?'#223329':plain?'#fffaf0':'#f6ebd5';g.fillRect(0,0,w,h);
 g.fillStyle=ink;g.font='bold 31px serif';g.fillText(appName,pad,65);
 let top=99,pw=w-2*pad,ph=Math.round(h*(o.ratio==='가로'?.43:.53));
 let refs=chosen.length?chosen:[cover(d)];let ims=await Promise.all(refs.slice(0,9).map(imageSource)),cols=ims.length===1?1:ims.length<=4?2:3,rows=Math.ceil(ims.length/cols),gap=8;
 if(dark){g.fillStyle='#ded7be';for(let x=pad;x<w-pad;x+=36){g.fillRect(x,top-15,16,7);g.fillRect(x,top+ph+8,16,7);}}
 ims.forEach((im,k)=>{let iw=(pw-(cols-1)*gap)/cols,ih=(ph-(rows-1)*gap)/rows;drawCover(g,im,pad+(k%cols)*(iw+gap),top+Math.floor(k/cols)*(ih+gap),iw,ih,o.zoom,o.cx,o.cy);});
 for(let m of o.masks||[]){g.fillStyle='#b6ab98';g.beginPath();g.arc(m.x*w,m.y*h,m.r*w,0,Math.PI*2);g.fill();}
 let y=top+ph+50;g.fillStyle=dark?'#d2ddb9':'#53774a';g.font='24px sans-serif';g.fillText([o.rate?(d.complete?'✓ 완료 인증':progress(d)+'% 진행'):'',o.date?displayDate(d):''].filter(Boolean).join('   ·   '),pad,y);
 g.fillStyle=ink;g.font='bold 48px serif';y=boundedText(g,d.title,pad,y+62,pw,2,57);g.font='30px sans-serif';if(o.note)boundedText(g,d.note,pad,y+6,pw,Math.max(1,Math.floor((h-y-20)/40)),40);return c;
}
let pending=new Map();window.nativeResult=(token,ok,text)=>{let x=pending.get(token);if(!x)return;pending.delete(token);if(ok)x.ok(text);else x.no(new Error(text));};
export async function output(blob,name,action='save'){
 let ext=name.split('.').pop();if(!window.Device){let a=document.createElement('a'),u=URL.createObjectURL(blob);a.href=u;a.download=name;a.click();setTimeout(()=>URL.revokeObjectURL(u),20000);return;}
 let t=Device.createFile(ext);if(!t)throw new Error('파일을 만들 수 없어요. 여유 공간을 확인해 주세요.');
 for(let i=0;i<blob.size;i+=196608){let url=await readURL(blob.slice(i,i+196608));if(!Device.append(t,url.slice(url.indexOf(',')+1)))throw new Error('파일 쓰기가 중단됐어요.');}
 let p=new Promise((ok,no)=>pending.set(t,{ok,no}));Device.finishFile(t,name,blob.type||'application/octet-stream',action);return p;
}
async function digest(blob){const raw=await readBytes(blob);const bytes=new Uint8Array(await crypto.subtle.digest('SHA-256',raw));return Array.from(bytes,n=>n.toString(16).padStart(2,'0')).join('');}
// Keep the format identifier shared with backups from earlier versions.
const backupFormatId = '꿈갈피';
export async function backup(){
 const parts=[],ds=T.dreams.slice(),memos=await all('memos'),media=Array.from(new Set(ds.reduce((a,d)=>a.concat(mediaKeys(d)),[]))),assets=[];let error;
 const zip=new Zip((e,chunk)=>{if(e)error=e;else parts.push(chunk);});
 async function entry(name,blob){const out=new ZipPassThrough(name);zip.add(out);for(let start=0;start<blob.size;start+=262144){out.push(await readBytes(blob.slice(start,start+262144)));if(error)throw error;}out.push(new Uint8Array(0),true);}
 for(let key of media){let blob=await get('media',key);if(!blob)throw Error('첨부 파일을 찾지 못했어요.');assets.push({key,type:blob.type,hash:await digest(blob)});await entry('media/'+key.slice(2),blob);}
 const manifest=new Blob([JSON.stringify({app:backupFormatId,version:2,dreams:ds,prefs:T.prefs,assets,memos})]);await entry('dreams.json',manifest);await entry('dreams.sha256',new Blob([await digest(manifest)]));zip.end();if(error)throw error;
 await output(new Blob(parts,{type:'application/zip'}),appName+'-백업-'+new Date().toISOString().slice(0,10)+'.zip');notice('꿈과 메모를 백업했어요.');
}
export async function restore(file){
 const entries=new Map(),names=new Set();let bad;const unzip=new Unzip(f=>{
  if(f.name!=='dreams.json'&&f.name!=='dreams.sha256'&&!/^media\/[a-f0-9]{32}$/.test(f.name))return;
  if(names.has(f.name)){bad=Error('백업에서 중복된 항목을 찾았어요.');return;}names.add(f.name);let chunks=[],size=0;
  f.ondata=(e,data,end)=>{if(e){bad=e;return;}size+=data.length;if(size>Math.max(file.size,32000000)){bad=Error('첨부 파일 크기를 확인할 수 없어요.');return;}chunks.push(data.slice());if(end){entries.set(f.name,new Blob(chunks));chunks=[];}};f.start();
 });unzip.register(UnzipInflate);
 for(let pos=0;pos<file.size;pos+=262144){unzip.push(await readBytes(file.slice(pos,pos+262144)),pos+262144>=file.size);if(bad)throw bad;}
 let main=entries.get('dreams.json'),hash=entries.get('dreams.sha256');if(!main||!hash||await digest(main)!==strFromU8(await readBytes(hash)))throw Error(appName+' 백업의 기록을 검증할 수 없어요.');
 let m=JSON.parse(strFromU8(await readBytes(main)));if(m.app!==backupFormatId||m.version!==2||!Array.isArray(m.dreams)||!Array.isArray(m.assets))throw Error('지원하는 백업 형식이 아니에요.');
 if(m.dreams.some(d=>typeof d.id!=='string'||typeof d.title!=='string'||!d.title.trim()))throw Error('백업의 꿈 기록을 확인할 수 없어요.');
 const memos=m.memos===undefined?[]:m.memos;
 if(!Array.isArray(memos)||!memos.every(validMemo)||new Set(memos.map(memo=>memo.id)).size!==memos.length)throw Error('백업의 메모를 확인할 수 없어요.');
 if(!confirm('꿈 '+m.dreams.length+'개, 메모 '+memos.length+'개, 첨부 '+m.assets.length+'개를 복원할까요? 같은 기록은 최근에 수정한 내용을 유지해요.'))return;
 let blobs=[];for(let a of m.assets){if(!/^m:[a-f0-9]{32}$/.test(a.key))throw Error('첨부 ID가 올바르지 않아요.');let e=entries.get('media/'+a.key.slice(2));if(!e||await digest(e)!==a.hash)throw Error('손상되거나 누락된 첨부가 있어요.');blobs.push([a.key,e.slice(0,e.size,a.type)]);}
 let available=new Set(blobs.map(x=>x[0]));if(m.dreams.some(d=>mediaKeys(normalize(d)).some(k=>!available.has(k))))throw Error('필요한 첨부가 모두 들어 있지 않아요.');
 await restoreData(m.dreams,m.prefs||{},blobs,memos);notice('복원했어요.');
}
export async function book(ds){
 if(!ds.length)throw new Error('책에 담을 꿈을 선택해 주세요.');let pdf=await PDFDocument.create();pdf.setTitle(appName+' · 나의 버킷리스트');let pageNo=0;
 async function sheet(c){let jpg=await pdf.embedJpg(await readBytes(await canvasBlob(c,.88))),p=pdf.addPage([595,842]);p.drawImage(jpg,{x:0,y:0,width:595,height:842});}
 function page(){const c=canvas(900,1274),g=c.getContext('2d');g.fillStyle='#fcf6e8';g.fillRect(0,0,900,1274);g.fillStyle='#34553d';g.font='bold 27px serif';g.fillText(appName,62,68);g.font='21px sans-serif';g.fillText(String(++pageNo),810,1218);return [c,g];}
 for(let d of ds){let [c,g]=page();g.font='bold 42px serif';let y=boundedText(g,d.title,62,145,776,3,54);g.font='22px sans-serif';g.fillText((d.complete?'완료 · ':'기록 · ')+displayDate(d),62,y+12);let im=await imageSource(cover(d));drawCover(g,im,62,y+44,776,450);y+=550;
 let texts=[d.why,d.note,d.recap,d.again,...d.logs.map(l=>stamp(l.at)+'  '+l.text)].filter(Boolean);for(let t of texts){g.font='26px sans-serif';for(let line of textLines(g,t,776)){if(y>1158){await sheet(c);[c,g]=page();g.font='26px sans-serif';y=132;}g.fillText(line,62,y);y+=39;}y+=17;}await sheet(c);
 let extras=allPhotos(d).filter(r=>r!==cover(d));for(let pos=0;pos<extras.length;pos+=4){[c,g]=page();g.font='bold 33px serif';boundedText(g,d.title,62,130,776,2,43);for(let j=0;j<Math.min(4,extras.length-pos);j++)drawCover(g,await imageSource(extras[pos+j]),62+(j%2)*394,230+Math.floor(j/2)*425,375,400);await sheet(c);}
 }await output(new Blob([await pdf.save()],{type:'application/pdf'}),appName+'-나의책.pdf');
}
export async function slideshow(d,photos){
 if(photos.length<2)throw new Error('슬라이드 영상에 담을 사진 두 장 이상을 선택해 주세요.');let ims=await Promise.all(photos.map(imageSource));
 if(window.Device?.beginMovie){let token=Device.beginMovie();if(token){
  let ready=false;
  try{for(let k=0;k<ims.length;k++){let frame=canvas(544,960),ctx=frame.getContext('2d');ctx.fillStyle='#f8f0df';ctx.fillRect(0,0,544,960);drawCover(ctx,ims[k],22,76,500,660);ctx.fillStyle='#284b34';ctx.font='bold 28px serif';ctx.fillText(appName,27,49);ctx.font='bold 34px serif';boundedText(ctx,d.title,26,796,488,2,42);ctx.font='20px sans-serif';ctx.fillText((k+1)+' / '+ims.length,28,915);if(!Device.movieFrame(token,k,frame.toDataURL('image/jpeg',.83).split(',')[1]))throw Error('프레임을 담지 못했어요.');}
   const encoding=new Promise((ok,no)=>pending.set(token,{ok,no}));Device.renderMovie(token);await encoding;ready=true;
  }catch(e){notice('이 기기에서 지원하는 영상 형식으로 다시 만들어요.');}
  if(ready){let saved=new Promise((ok,no)=>pending.set(token,{ok,no}));Device.finishFile(token,appName+'-슬라이드.mp4','video/mp4','save');return saved;}
 }}
 let c=canvas(540,960),g=c.getContext('2d');if(!c.captureStream||!window.MediaRecorder)throw new Error('Android System WebView를 업데이트하면 영상을 만들 수 있어요.');
 let mime=['video/mp4;codecs=avc1.42E01E','video/webm;codecs=vp8','video/webm'].find(t=>MediaRecorder.isTypeSupported(t));if(!mime)throw new Error('이 기기에서 지원하는 영상 인코더가 없어요.');
 let stream=c.captureStream(15),rec=new MediaRecorder(stream,{mimeType:mime,videoBitsPerSecond:1600000}),chunks=[];
 let blob=new Promise((ok,no)=>{rec.ondataavailable=e=>{if(e.data.size)chunks.push(e.data)};rec.onstop=()=>ok(new Blob(chunks,{type:mime.split(';')[0]}));rec.onerror=()=>no(new Error('영상 인코딩에 실패했어요.'));});
 function frame(k,phase){g.fillStyle='#f7efdc';g.fillRect(0,0,540,960);drawCover(g,ims[k],24,80,492,650,1+.04*phase);g.fillStyle='#254c35';g.font='bold 27px serif';g.fillText(appName,28,51);g.font='bold 34px serif';boundedText(g,d.title,26,795,485,2,42);g.font='21px sans-serif';g.fillText((k+1)+' / '+ims.length,30,915);}
 frame(0,0);rec.start(250);try{for(let k=0;k<ims.length;k++){for(let n=0;n<18;n++){frame(k,n/18);await new Promise(r=>setTimeout(r,100));}}}finally{if(rec.state!=='inactive')rec.stop();stream.getTracks().forEach(t=>t.stop());}
 let result=await blob;if(!result.size)throw new Error('영상에 프레임이 없어요.');await output(result,appName+'-슬라이드.'+(mime.includes('mp4')?'mp4':'webm'));
}
