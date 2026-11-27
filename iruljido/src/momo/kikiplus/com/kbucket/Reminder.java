package momo.kikiplus.com.kbucket;
import android.app.*;import android.content.*;import android.net.Uri;import java.util.*;import org.json.*;
public class Reminder extends BroadcastReceiver{
 static final String FIRE="momo.kikiplus.com.kbucket.REMIND";
 static SharedPreferences prefs(Context c){return c.getSharedPreferences("v2",0);}
 static JSONArray schedules(Context c){try{return new JSONArray(prefs(c).getString("schedule","[]"));}catch(Exception e){return new JSONArray();}}
 static PendingIntent pending(Context c,JSONObject o){Intent i=new Intent(c,Reminder.class).setAction(FIRE).setData(Uri.fromParts("dreamalarm",o.optString("key"),null));return PendingIntent.getBroadcast(c,0,i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 static void sync(Context c,String json)throws Exception{
  new JSONArray(json);if(json.equals(prefs(c).getString("schedule","")))return;
  AlarmManager am=c.getSystemService(AlarmManager.class);JSONArray old=schedules(c);for(int i=0;i<old.length();i++)am.cancel(pending(c,old.getJSONObject(i)));
  prefs(c).edit().putString("schedule",json).apply();reschedule(c);
 }
 static long next(JSONObject o,long after){long start=o.optLong("when");String r=o.optString("repeat");Calendar date=Calendar.getInstance();date.setTimeInMillis(start);if(start<=0)return 0;
  if(r.equals("weekly")||r.equals("yearly")||r.equals("daily")){while(date.getTimeInMillis()<=after){if(r.equals("yearly"))date.add(Calendar.YEAR,1);else date.add(Calendar.DATE,r.equals("weekly")?7:1);}return date.getTimeInMillis();}
  return start>after?start:0;
 }
 static void reschedule(Context c){JSONArray arr=schedules(c);AlarmManager am=c.getSystemService(AlarmManager.class);for(int k=0;k<arr.length();k++){JSONObject o=arr.optJSONObject(k);if(o==null)continue;long t=next(o,System.currentTimeMillis());if(t>0)am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,t,pending(c,o));}}
 static void registerChannel(Context c,NotificationManager manager){
  manager.createNotificationChannel(new NotificationChannel("dreams",c.getString(R.string.app_name)+" 일정",NotificationManager.IMPORTANCE_DEFAULT));
 }
 public void onReceive(Context c,Intent i){
  if(Intent.ACTION_MY_PACKAGE_REPLACED.equals(i.getAction())){
   NotificationManager manager=c.getSystemService(NotificationManager.class);
   if(manager.getNotificationChannel("dreams")!=null)registerChannel(c,manager);
  }
  if(!FIRE.equals(i.getAction())){reschedule(c);DreamWidget.refresh(c);return;}
  String key=i.getData()==null?"":i.getData().getSchemeSpecificPart();JSONArray all=schedules(c);
  for(int k=0;k<all.length();k++){JSONObject o=all.optJSONObject(k);if(o==null||!key.equals(o.optString("key")))continue;
   NotificationManager nm=c.getSystemService(NotificationManager.class);registerChannel(c,nm);
   PendingIntent pi=PendingIntent.getActivity(c,key.hashCode(),new Intent(c,Home.class).putExtra("dream",o.optString("dream")),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
   boolean hide=prefs(c).getBoolean("lock",false);
   try{nm.notify(key,1,new Notification.Builder(c,"dreams").setSmallIcon(R.drawable.icon).setContentTitle(hide?c.getString(R.string.app_name):o.optString("title")).setContentText(hide?"꿈 수첩에 일정이 있어요.":o.optString("text")).setContentIntent(pi).setAutoCancel(true).build());}catch(SecurityException ignored){}
   if(!o.optString("repeat").isEmpty()){long t=next(o,System.currentTimeMillis()+60000);if(t>0)c.getSystemService(AlarmManager.class).setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,t,pending(c,o));}break;
  }
 }
}
