package momo.kikiplus.com.kbucket;
import android.appwidget.*;import android.app.*;import android.content.*;import android.widget.*;
public class DreamWidget extends AppWidgetProvider {
 public void onUpdate(Context c,AppWidgetManager m,int[] ids){refresh(c);}
 static void refresh(Context c){
  SharedPreferences p=c.getSharedPreferences("v2",0);boolean locked=p.getBoolean("lock",false);
  RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget);
  String appName=c.getString(R.string.app_name);
  String dreamId=p.getString("widgetId","");
  v.setTextViewText(R.id.widget_title,locked || dreamId.isEmpty() ? appName : p.getString("widgetTitle",appName));
  v.setTextViewText(R.id.widget_next,locked?"잠금을 해제해 꿈 수첩을 열어 보세요.":p.getString("widgetNext","첫 꿈을 적어 보세요."));
  Intent i=new Intent(c,Home.class).putExtra("dream",dreamId);
  v.setOnClickPendingIntent(R.id.widget_root,PendingIntent.getActivity(c,31,i,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT));
  AppWidgetManager.getInstance(c).updateAppWidget(new ComponentName(c,DreamWidget.class),v);
 }
}
