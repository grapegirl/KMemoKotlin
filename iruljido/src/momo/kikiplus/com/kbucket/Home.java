package momo.kikiplus.com.kbucket;
import android.Manifest;
import android.app.*;
import android.appwidget.AppWidgetManager;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.biometrics.*;
import android.media.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.security.keystore.*;
import android.util.Base64;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.security.*;
import java.util.*;
import java.util.concurrent.*;
import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import org.json.*;

public class Home extends Activity {
 private HomeAdBanner homeBanner;
 WebView web; boolean consumedIntent; ValueCallback<Uri[]> chooser; Uri capture; FrameLayout root; LinearLayout gate;
 final ExecutorService jobs=Executors.newSingleThreadExecutor();
 android.content.SharedPreferences prefs; volatile boolean unlocked; boolean authenticating,enableLock,loaded,active;
 String inbox="",destName,pendingToken; File pendingFile,recordFile; MediaRecorder recorder;
 final Map<String,File> staged=new ConcurrentHashMap<>();
 final Map<String,OutputStream> streams=new ConcurrentHashMap<>();
 boolean memoWindow(){return false;}
 boolean locked(){return prefs.getBoolean("lock",false);}
 void js(String code){runOnUiThread(()->{if(loaded&&!isDestroyed())web.evaluateJavascript(code,null);});}
 void message(String m){js("window.notice&&notice("+JSONObject.quote(m)+")");}
 void fail(String m){runOnUiThread(()->Toast.makeText(this,m,Toast.LENGTH_LONG).show());}
 public void onCreate(Bundle bundle){
  super.onCreate(bundle);prefs=getSharedPreferences("v2",0);
  getWindow().setStatusBarColor(0xfffcf7ee);getWindow().setNavigationBarColor(0xfffcf7ee);
  getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
  root=new FrameLayout(this);setContentView(root);web=new WebView(this);
  if(memoWindow()) {
   root.addView(web);
  } else {
   LinearLayout column=new LinearLayout(this);
   column.setOrientation(LinearLayout.VERTICAL);
   root.addView(column);
   column.addView(web,new LinearLayout.LayoutParams(-1,0,1f));
   homeBanner=new HomeAdBanner(column);
  }
  gate=new LinearLayout(this);gate.setOrientation(1);gate.setGravity(Gravity.CENTER);gate.setBackgroundColor(0xfffcf7ee);
  ImageView icon=new ImageView(this);icon.setImageResource(R.drawable.icon);int size=(int)(getResources().getDisplayMetrics().density*76);gate.addView(icon,size,size);
  Button open=new Button(this);open.setText(getString(R.string.app_name) + " 잠금 해제");open.setOnClickListener(v->authenticate(false));gate.addView(open);root.addView(gate);
  unlocked=!locked();gate.setVisibility(unlocked?View.GONE:View.VISIBLE);
  if(locked())getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
  WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowFileAccessFromFileURLs(false);s.setAllowUniversalAccessFromFileURLs(false);s.setAllowContentAccess(true);s.setSupportMultipleWindows(false);s.setTextZoom(100);s.setUserAgentString(s.getUserAgentString()+" Iruljido/"+BuildConfig.VERSION_NAME);
  web.addJavascriptInterface(new Bridge(),"Device");
  web.setWebViewClient(new WebViewClient(){
   public boolean shouldOverrideUrlLoading(WebView w,WebResourceRequest req){return true;}
   public WebResourceResponse shouldInterceptRequest(WebView w,WebResourceRequest req){
    Uri u=req.getUrl();if("file".equals(u.getScheme())&&u.getPath()!=null&&u.getPath().startsWith("/android_asset/")&&!u.getPath().contains(".."))return null;
    if("https".equals(u.getScheme())||"http".equals(u.getScheme())||"file".equals(u.getScheme()))return new WebResourceResponse("text/plain","utf-8",new ByteArrayInputStream(new byte[0]));
    return null;
   }
  });
  web.setWebChromeClient(new WebChromeClient(){
   public boolean onShowFileChooser(WebView w,ValueCallback<Uri[]> cb,FileChooserParams p){if(chooser!=null)chooser.onReceiveValue(null);chooser=cb;
    String mime="*/*";for(String t:p.getAcceptTypes())if(t!=null&&t.contains("/")){mime=t.split(",")[0];break;}
    try{
     if(p.isCaptureEnabled()&&mime.startsWith("image/")){
      capture=Images.fileUri(Home.this,"capture",UUID.randomUUID()+".jpg");Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);i.putExtra(MediaStore.EXTRA_OUTPUT,capture);i.setClipData(ClipData.newRawUri("사진",capture));i.addFlags(3);startActivityForResult(i,2);
     }else if(p.isCaptureEnabled()&&mime.startsWith("video/")){
      startActivityForResult(new Intent(MediaStore.ACTION_VIDEO_CAPTURE).putExtra(MediaStore.EXTRA_DURATION_LIMIT,45).putExtra(MediaStore.EXTRA_SIZE_LIMIT,32L*1024*1024),3);
     }else{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT).setType(mime).addCategory(Intent.CATEGORY_OPENABLE);i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,p.getMode()==FileChooserParams.MODE_OPEN_MULTIPLE);startActivityForResult(i,1);}
    }catch(Exception e){chooser.onReceiveValue(null);chooser=null;message("파일을 열 앱이 없어요. 앨범 또는 다른 파일을 선택해 주세요.");}return true;
   }
  });
  if(memoWindow() || bundle==null || !bundle.getBoolean("consumedIntent"))acceptIntent(getIntent());else consumedIntent=true;
  if(unlocked)load();
  File[] files=getCacheDir().listFiles();if(files!=null)for(File f:files)if(f.getName().matches("[0-9a-f-]{36}\\.(jpg|zip|mp4|webm|pdf|m4a)")&&System.currentTimeMillis()-f.lastModified()>86400000L)f.delete();
 }
 void load(){if(!loaded){loaded=true;web.loadUrl("file:///android_asset/index.html" + (memoWindow() ? "#memo" : ""));}}
 protected synchronized void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);out.putBoolean("consumedIntent",consumedIntent);}
 synchronized void acceptIntent(Intent intent){
  if(intent==null)return;
  String data=null;
  if(Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType())){
   CharSequence text;
   try{text=intent.getCharSequenceExtra(Intent.EXTRA_TEXT);}catch(RuntimeException e){return;}
   if(text!=null)data=text.subSequence(0,Math.min(8192,text.length())).toString();
  }else if(intent.hasExtra("dream")){
   String id=intent.getStringExtra("dream");
   if(id!=null)data="dream:"+id;
  }
  if(data==null||data.isEmpty())return;
  consumedIntent=false;
  inbox=data;
  if(unlocked)js("window.externalInput&&externalInput()");
 }
 synchronized String takeInbox(){
  if(!unlocked||inbox.isEmpty())return "";
  String value=inbox;
  inbox="";
  consumedIntent=true;
  return value;
 }
 protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);acceptIntent(i);}
 private void syncAdForeground(){
  if(homeBanner!=null)homeBanner.setForeground(active && unlocked && !authenticating);
 }
 @Override protected void onPause(){
  active=false;
  syncAdForeground();
  super.onPause();
 }
 protected void onResume(){
  super.onResume();active=true;
  if(locked()){
   getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
   if(!unlocked){if(!authenticating)authenticate(false);return;}
  }else{
   getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
   unlocked=true;gate.setVisibility(View.GONE);web.setVisibility(View.VISIBLE);load();
  }
  syncAdForeground();
  js("window.appResume&&appResume()");
 }
 protected void onStop(){super.onStop();active=false;if(recorder!=null)stopVoice();if((locked()||memoWindow())&&!authenticating){unlocked=false;gate.setVisibility(View.VISIBLE);web.setVisibility(View.INVISIBLE);}}
 void authenticated(boolean yes){
  authenticating=false;
  if(yes){if(enableLock){prefs.edit().putBoolean("lock",true).apply();getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);DreamWidget.refresh(this);MemoWidget.refresh(this);}unlocked=true;gate.setVisibility(View.GONE);web.setVisibility(View.VISIBLE);load();js("window.appResume&&appResume()");}
  else if(locked()){gate.setVisibility(View.VISIBLE);web.setVisibility(View.INVISIBLE);}
  enableLock=false;
  syncAdForeground();
 }
 void authenticate(boolean enable){
  if(authenticating)return;KeyguardManager km=getSystemService(KeyguardManager.class);
  if(!km.isDeviceSecure()){fail("먼저 휴대폰 설정에서 화면 잠금을 설정해 주세요.");return;}
  enableLock=enable;authenticating=true;syncAdForeground();
  if(Build.VERSION.SDK_INT>=30){try{
   BiometricPrompt prompt=new BiometricPrompt.Builder(this).setTitle(getString(R.string.app_name) + " 잠금 해제").setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG|BiometricManager.Authenticators.DEVICE_CREDENTIAL).build();
   prompt.authenticate(new CancellationSignal(),getMainExecutor(),new BiometricPrompt.AuthenticationCallback(){
    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult r){authenticated(true);}
    public void onAuthenticationError(int e,CharSequence m){authenticated(false);}
   });return;
  }catch(Exception ignored){}}
  Intent i=km.createConfirmDeviceCredentialIntent(getString(R.string.app_name),"휴대폰 잠금을 해제해 주세요.");if(i==null){authenticated(false);return;}startActivityForResult(i,7);
 }
 void callback(String token,boolean ok,String result){js("window.nativeResult&&nativeResult("+JSONObject.quote(token)+","+ok+","+JSONObject.quote(result)+")");}
 protected void onActivityResult(int req,int result,Intent i){
  super.onActivityResult(req,result,i);if(req==7){authenticated(result==RESULT_OK);return;}
  if(req==5){File f=pendingFile;String token=pendingToken;pendingFile=null;pendingToken=null;
   if(f==null)return;if(result!=RESULT_OK||i==null||i.getData()==null){callback(token,false,"저장을 취소했어요.");return;}
   Uri u=i.getData();jobs.execute(()->{try(InputStream in=new FileInputStream(f);OutputStream out=getContentResolver().openOutputStream(u,"wt")){byte[] b=new byte[65536];int n;while((n=in.read(b))!=-1)out.write(b,0,n);out.flush();callback(token,true,"저장했어요.");}catch(Exception e){callback(token,false,"파일을 저장하지 못했어요.");}});return;
  }
  if(chooser==null)return;ArrayList<Uri> list=new ArrayList<>();if(result==RESULT_OK){
   if(req==2&&capture!=null)list.add(capture);else if(i!=null){if(i.getClipData()!=null){for(int k=0;k<i.getClipData().getItemCount();k++)list.add(i.getClipData().getItemAt(k).getUri());}else if(i.getData()!=null)list.add(i.getData());}
  }list.removeIf(u->u==null||!"content".equals(u.getScheme())||(req!=2&&Images.authority(this).equals(u.getAuthority())));
  chooser.onReceiveValue(list.isEmpty()?null:list.toArray(new Uri[0]));chooser=null;
 }
 void startVoice(){try{if(recorder!=null)return;recordFile=new File(getCacheDir(),UUID.randomUUID()+".m4a");recorder=new MediaRecorder();recorder.setAudioSource(MediaRecorder.AudioSource.MIC);recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);recorder.setAudioEncodingBitRate(64000);recorder.setOutputFile(recordFile.toString());recorder.setMaxDuration(300000);recorder.setOnInfoListener((r,w,x)->{if(w==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED)stopVoice();});recorder.prepare();recorder.start();js("window.recordState&&recordState(true)");}catch(Exception e){if(recorder!=null)recorder.release();recorder=null;message("마이크를 시작하지 못했어요.");}}
 void stopVoice(){if(recorder==null)return;MediaRecorder r=recorder;recorder=null;try{r.stop();byte[] b=java.nio.file.Files.readAllBytes(recordFile.toPath());js("window.voiceReady&&voiceReady("+JSONObject.quote(Base64.encodeToString(b,Base64.NO_WRAP))+")");}catch(Exception e){message("녹음이 너무 짧거나 저장할 수 없어요.");}finally{r.release();recordFile.delete();js("window.recordState&&recordState(false)");}}
 public void onRequestPermissionsResult(int r,String[] p,int[] v){super.onRequestPermissionsResult(r,p,v);if(r==21){if(v.length>0&&v[0]==0)startVoice();else message("음성 메모에는 마이크 권한이 필요해요.");}if(r==22&&v.length>0&&v[0]!=0)message("일정은 저장됐어요. 알림을 받으려면 앱 설정에서 알림을 켜 주세요.");}
 public void onBackPressed(){if(unlocked)js("window.back&&back()");else finish();}
 protected void onDestroy(){
  if(homeBanner!=null){homeBanner.release();homeBanner=null;}
  if(recorder!=null){recorder.release();recorder=null;}jobs.shutdownNow();for(OutputStream o:streams.values())try{o.close();}catch(Exception ignored){}
  if(memoWindow() && web!=null){
   loaded=false;
   web.removeJavascriptInterface("Device");
   root.removeView(web);
   web.destroy();
  }
  super.onDestroy();
 }
 void shareIntent(Intent i,boolean kakao){
  if(kakao){i.setPackage("com.kakao.talk");startActivity(i);return;}
  Intent chooser=Intent.createChooser(i,getString(R.string.app_name) + " 공유");
  chooser.putExtra(Intent.EXTRA_EXCLUDE_COMPONENTS,new ComponentName[]{new ComponentName(this,Home.class)});
  startActivity(chooser);
 }
 synchronized String secret()throws Exception{
  KeyStore ks=KeyStore.getInstance("AndroidKeyStore");ks.load(null);String alias="dream-remote-store";
  if(!ks.containsAlias(alias)){KeyGenerator gen=KeyGenerator.getInstance("AES","AndroidKeyStore");gen.init(new KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());gen.generateKey();}
  java.security.Key key=ks.getKey(alias,null);Cipher cipher=Cipher.getInstance("AES/GCM/NoPadding");String value=prefs.getString("identity","");
  if(value.isEmpty()){byte[] raw=new byte[32];new SecureRandom().nextBytes(raw);cipher.init(Cipher.ENCRYPT_MODE,key);byte[] enc=cipher.doFinal(raw);prefs.edit().putString("identity",Base64.encodeToString(cipher.getIV(),2)+"."+Base64.encodeToString(enc,2)).commit();return Base64.encodeToString(raw,2);}
  String[] parts=value.split("\\.");cipher.init(Cipher.DECRYPT_MODE,key,new GCMParameterSpec(128,Base64.decode(parts[0],2)));return Base64.encodeToString(cipher.doFinal(Base64.decode(parts[1],2)),2);
 }
 class Bridge {
  @JavascriptInterface public void setHomeBannerVisible(boolean visible){
   runOnUiThread(()->{if(homeBanner!=null)homeBanner.setHomeVisible(visible);});
  }
  @JavascriptInterface public String takeInput(){return memoWindow() ? "" : takeInbox();}
  @JavascriptInterface public boolean isMemoWindow(){return memoWindow();}
  @JavascriptInterface public String takeMemoTarget(){return memoWindow() ? takeInbox() : "";}
  @JavascriptInterface public void openMemoBook(){
   if(unlocked) runOnUiThread(()->startActivity(MemoWidget.open(Home.this, MemoWidget.LIST)));
  }
  @JavascriptInterface public void memoKeyboard(){
   runOnUiThread(()->{
    if(!memoWindow() || !active || !unlocked || isFinishing()) return;
    web.requestFocus();
    getSystemService(android.view.inputmethod.InputMethodManager.class).showSoftInput(web, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
   });
  }
  @JavascriptInterface public void memoPreview(long revision, String id, String preview){
   if(!unlocked || revision<0 || id==null || preview==null || preview.length()>512) return;
   if(!id.isEmpty() && !id.matches("[a-f0-9]{32}")) return;
   MemoWidget.cache(Home.this,revision,id,preview);
  }
  @JavascriptInterface public String key(){if(!unlocked)return "";try{return secret();}catch(Exception e){return "";}}
  @JavascriptInterface public boolean lockEnabled(){return locked();}
  @JavascriptInterface public void setLock(boolean on){if(!unlocked)return;runOnUiThread(()->{if(on)authenticate(true);else{prefs.edit().putBoolean("lock",false).apply();getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);DreamWidget.refresh(Home.this);MemoWidget.refresh(Home.this);js("window.appResume&&appResume()");}});}
  @JavascriptInterface public void exit(){runOnUiThread(()->finish());}
  @JavascriptInterface public String card(String id){if(!unlocked)return "";try{return Base64.encodeToString(java.nio.file.Files.readAllBytes(new File(new File(getFilesDir(),"cards"),UUID.fromString(id)+".jpg").toPath()),2);}catch(Exception e){return "";}}
  @JavascriptInterface public String people(){return unlocked?Friends.archive(Home.this):"{}";}
  @JavascriptInterface public void copy(String s){if(unlocked)runOnUiThread(()->{getSystemService(android.content.ClipboardManager.class).setPrimaryClip(ClipData.newPlainText(getString(R.string.app_name),s));message("복사했어요.");});}
  @JavascriptInterface public void textShare(String s,boolean k){if(unlocked)runOnUiThread(()->{try{shareIntent(new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,s),k);}catch(Exception e){message("공유할 앱을 열지 못했어요.");}});}
  @JavascriptInterface public void map(String query){if(unlocked)runOnUiThread(()->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q="+Uri.encode(query))));}catch(Exception e){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/maps/search/?api=1&query="+Uri.encode(query))));}catch(Exception ignored){message("지도를 열 수 없어요.");}}});}
  @JavascriptInterface public void record(boolean start){if(unlocked)runOnUiThread(()->{if(!start){stopVoice();return;}if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},21);else startVoice();});}
  @JavascriptInterface public String beginMovie(){if(!unlocked)return "";String k=UUID.randomUUID().toString();File dir=new File(getCacheDir(),"slides-"+k);return dir.mkdir()?k:"";}
  @JavascriptInterface public boolean movieFrame(String k,int index,String jpeg){
   if(!unlocked||k==null||!k.matches("[0-9a-f-]{36}")||index<0||jpeg.length()>3000000)return false;
   File dir=new File(getCacheDir(),"slides-"+k);if(!dir.isDirectory())return false;
   try(FileOutputStream o=new FileOutputStream(new File(dir,String.format(Locale.US,"%08d.jpg",index)))){o.write(Base64.decode(jpeg,Base64.DEFAULT));return true;}catch(Exception e){return false;}
  }
  @JavascriptInterface public void renderMovie(String k){if(!unlocked||!k.matches("[0-9a-f-]{36}"))return;jobs.execute(()->{
   File dir=new File(getCacheDir(),"slides-"+k),out=new File(getCacheDir(),k+".mp4");
   try{Movie.encode(dir,out);staged.put(k,out);streams.put(k,new ByteArrayOutputStream());callback(k,true,"");}
   catch(Exception e){out.delete();callback(k,false,"이 기기의 MP4 인코더를 열지 못했어요.");}
   finally{File[] files=dir.listFiles();if(files!=null)for(File f:files)f.delete();dir.delete();}
  });}
  @JavascriptInterface public String createFile(String ext){if(!unlocked||!ext.matches("jpg|pdf|zip|mp4|webm"))return "";try{String token=UUID.randomUUID().toString();File f=new File(getCacheDir(),token+"."+ext);streams.put(token,new FileOutputStream(f));staged.put(token,f);return token;}catch(Exception e){return "";}}
  @JavascriptInterface public boolean append(String token,String b64){try{OutputStream o=streams.get(token);if(!unlocked||o==null||b64.length()>600000)return false;o.write(Base64.decode(b64,0));return true;}catch(Exception e){return false;}}
  @JavascriptInterface public void finishFile(String token,String name,String mime,String action){if(!unlocked){callback(token,false,"잠금을 해제한 뒤 다시 보내 주세요.");return;}
   try{OutputStream o=streams.remove(token);File f=staged.remove(token);if(o==null||f==null)throw new IOException();o.close();
    runOnUiThread(()->{try{if("save".equals(action)){if(pendingFile!=null)throw new IOException();pendingFile=f;pendingToken=token;startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType(mime).putExtra(Intent.EXTRA_TITLE,name),5);}
     else{Uri uri=Images.fileUri(Home.this,"share",f.getName());Intent i=new Intent(Intent.ACTION_SEND).setType(mime).putExtra(Intent.EXTRA_STREAM,uri).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);i.setClipData(ClipData.newRawUri(name,uri));shareIntent(i,"kakao".equals(action));callback(token,true,"");}
    }catch(Exception e){if(token.equals(pendingToken)){pendingFile=null;pendingToken=null;}callback(token,false,"kakao".equals(action)?"카카오톡을 열 수 없어요. 카카오톡 설치를 확인하거나 다른 앱으로 공유해 주세요.":"공유할 앱이나 저장 위치를 열지 못했어요.");}});
   }catch(Exception e){callback(token,false,"파일을 준비하지 못했어요.");}
  }
  @JavascriptInterface public void alarms(String json){jobs.execute(()->{try{Reminder.sync(Home.this,json);}catch(Exception e){message("일정 알림을 예약하지 못했어요.");}});}
  @JavascriptInterface public void notifications(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission("android.permission.POST_NOTIFICATIONS")!=0)runOnUiThread(()->requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"},22));}
  @JavascriptInterface public void widget(String id,String title,String next){prefs.edit().putString("widgetId",id).putString("widgetTitle",title).putString("widgetNext",next).apply();DreamWidget.refresh(Home.this);}
  @JavascriptInterface public void pinWidget(){requestWidget(DreamWidget.class, R.string.progress_widget_name);}
  @JavascriptInterface public void pinMemoWidget(){requestWidget(MemoWidget.class, R.string.memo_widget_name);}
  private void requestWidget(Class<?> provider, int label){
   if(!unlocked) return;
   runOnUiThread(()->{
    AppWidgetManager manager=AppWidgetManager.getInstance(Home.this);
    try{if(manager.isRequestPinAppWidgetSupported() && manager.requestPinAppWidget(new ComponentName(Home.this,provider),null,null)) return;}catch(IllegalStateException | SecurityException ignored){}
    message("휴대폰 홈 화면을 길게 누르고 위젯 → " + getString(R.string.app_name) + " → "+getString(label)+"를 선택하세요.");
   });
  }
 }
}
