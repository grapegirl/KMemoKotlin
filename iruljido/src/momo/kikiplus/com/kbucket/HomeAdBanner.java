package momo.kikiplus.com.kbucket;

import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.kakao.adfit.ads.AdListener;
import com.kakao.adfit.ads.ba.BannerAdView;

/** AdFit's view has its own native surface, separate from the journal WebView. */
final class HomeAdBanner extends FrameLayout {
    private static final long RETRY_INTERVAL_MS = 60_000L;
    private final LinearLayout column;
    private final int minimumWidth;
    private final int spacing;
    private BannerAdView banner;
    private boolean homeVisible, foreground, showing, released, requestFailed;
    private long lastRequest;

    HomeAdBanner(LinearLayout column) {
        super(column.getContext());
        this.column = column;
        float density = getResources().getDisplayMetrics().density;
        minimumWidth = Math.round(320 * density);
        spacing = Math.round(8 * density);
        setVisibility(GONE);
        setBackgroundColor(0xfffcf7ed);
        column.addView(this, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        column.addOnLayoutChangeListener(widthListener);
    }

    private final View.OnLayoutChangeListener widthListener =
            (view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
                if (right - left != oldRight - oldLeft) post(this::update);
            };

    void setHomeVisible(boolean visible) {
        homeVisible = visible;
        update();
    }

    void setForeground(boolean active) {
        foreground = active;
        update();
    }

    private void update() {
        if (released) return;
        boolean eligible = homeVisible && foreground && column.getWidth() >= minimumWidth;
        if (showing == eligible) return;
        showing = eligible;
        if (!eligible) {
            setVisibility(GONE);
            banner.pause();
            return;
        }

        setVisibility(VISIBLE);
        boolean firstVisit = banner == null;
        if (firstVisit) {
            banner = new BannerAdView(getContext());
            banner.setAdUnitId(getResources().getString(R.string.home_banner_unit));
            banner.setTestMode(BuildConfig.DEBUG);
            banner.setAdListener(new AdListener() {
                @Override public void onAdLoaded() {
                    requestFailed = false;
                    if (BuildConfig.DEBUG) Log.d("IruljidoAdFit", "Home banner loaded");
                }
                @Override public void onAdFailed(int code) {
                    requestFailed = true;
                    if (BuildConfig.DEBUG) Log.d("IruljidoAdFit", "Home banner unavailable: " + code);
                }
            });
            addView(banner, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));
        }
        banner.resume();
        long now = SystemClock.elapsedRealtime();
        if (firstVisit || (requestFailed && now - lastRequest >= RETRY_INTERVAL_MS)) {
            requestFailed = false;
            lastRequest = now;
            banner.loadAd();
        }
    }

    @Override protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        // Keep distance from the navigation buttons only when an ad occupies the slot.
        if (getMeasuredHeight() > 0) {
            setMeasuredDimension(getMeasuredWidthAndState(),
                    resolveSize(getMeasuredHeight() + spacing, heightSpec));
        }
    }

    void release() {
        released = true;
        column.removeOnLayoutChangeListener(widthListener);
        if (banner != null) {
            banner.destroy();
            banner = null;
        }
    }
}
