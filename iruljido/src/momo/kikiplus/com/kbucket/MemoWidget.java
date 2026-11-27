package momo.kikiplus.com.kbucket;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

public class MemoWidget extends AppWidgetProvider {
    static final String NEW = "new";
    static final String LIST = "list";
    private static final String MEMO_ACTION = "momo.kikiplus.com.kbucket.OPEN_MEMO";

    static Intent open(Context context, String target) {
        Uri uri = new Uri.Builder().scheme("kkumgalpi-widget").authority("memo")
                .appendPath(target).build();
        return new Intent(context, MemoWindow.class).setAction(MEMO_ACTION).setData(uri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    static String target(Intent intent) {
        Uri uri = intent == null ? null : intent.getData();
        if (uri == null || !MEMO_ACTION.equals(intent.getAction()) ||
                !"kkumgalpi-widget".equals(uri.getScheme()) || !"memo".equals(uri.getAuthority())) {
            return LIST;
        }
        String value = uri.getLastPathSegment();
        if (NEW.equals(value) || (value != null && value.matches("[a-f0-9]{32}"))) return value;
        return LIST;
    }

    private static PendingIntent click(Context context, String target) {
        return PendingIntent.getActivity(context, 32, open(context, target),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] widgetIds) {
        refresh(context);
    }

    // A normal app window and a widget editor can both read the same IndexedDB.
    // Only the most recent committed database revision should be shown by the launcher.
    static synchronized void cache(Context context, long revision, String id, String preview) {
        SharedPreferences prefs = context.getSharedPreferences("v2", Context.MODE_PRIVATE);
        long cached = prefs.getLong("memoRevision", -1L);
        if (revision < cached || (revision == cached && id.equals(prefs.getString("memoId", ""))
                && preview.equals(prefs.getString("memoPreview", "")))) return;
        prefs.edit().putLong("memoRevision", revision).putString("memoId", id)
                .putString("memoPreview", preview).apply();
        refresh(context);
    }

    static synchronized void refresh(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("v2", Context.MODE_PRIVATE);
        boolean locked = prefs.getBoolean("lock", false);
        String id = prefs.getString("memoId", "");
        String content = locked ? context.getString(R.string.memo_locked)
                : id.isEmpty() ? context.getString(R.string.memo_empty)
                : prefs.getString("memoPreview", "");
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.memo_widget);
        views.setTextViewText(R.id.memo_preview, content);
        views.setViewVisibility(R.id.memo_caption, id.isEmpty() || locked ? View.GONE : View.VISIBLE);
        views.setOnClickPendingIntent(R.id.memo_book, click(context, LIST));
        views.setOnClickPendingIntent(R.id.memo_write, click(context, NEW));
        views.setOnClickPendingIntent(R.id.memo_body, click(context, id.isEmpty() ? NEW : id));
        AppWidgetManager.getInstance(context).updateAppWidget(new ComponentName(context, MemoWidget.class), views);
    }
}
