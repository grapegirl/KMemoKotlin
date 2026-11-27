package momo.kikiplus.com.kbucket;

import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kakao.adfit.ads.AdListener;
import com.kakao.adfit.ads.ba.BannerAdView;

/** The bottom row belongs to the activity, so scrolling and changing tabs cannot move it. */
final class BottomAdBanner extends FrameLayout implements AdListener {
    private static final long RETRY_DELAY_MS = 60_000L;
    private final int bannerWidth;
    private final TextView placeholder;
    private BannerAdView banner;
    private boolean contentReady, foreground, running, hasAd, released;
    private long retryAt;

    BottomAdBanner(LinearLayout column) {
        super(column.getContext());
        float density = getResources().getDisplayMetrics().density;
        bannerWidth = Math.round(320 * density);
        int bannerHeight = Math.round(50 * density);
        int gap = Math.round(8 * density);
        setPadding(0, gap, 0, 0);
        setBackgroundColor(0xfffcf7ed);
        placeholder = new TextView(getContext());
        placeholder.setText(R.string.banner_placeholder);
        placeholder.setTextColor(0xff736d60);
        placeholder.setTextSize(12);
        placeholder.setGravity(Gravity.CENTER);
        addView(placeholder, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        column.addView(this, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, gap + bannerHeight));
    }

    void onContentReady() {
        contentReady = true;
        update();
    }

    void setAppForeground(boolean active) {
        foreground = active;
        update();
    }

    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w != oldw) post(this::update);
    }

    private void update() {
        if (released) return;
        boolean fits = getWidth() >= bannerWidth;
        placeholder.setVisibility(hasAd && fits ? INVISIBLE : VISIBLE);
        if (banner != null) banner.setVisibility(fits ? VISIBLE : INVISIBLE);
        boolean eligible = fits && foreground && contentReady;
        if (running == eligible) return;
        running = eligible;
        if (!running) {
            removeCallbacks(retry);
            banner.pause();
            return;
        }
        if (banner == null) {
            banner = new BannerAdView(getContext());
            banner.setAdUnitId(getResources().getString(R.string.home_banner_unit));
            banner.setTestMode(BuildConfig.DEBUG);
            banner.setAdListener(this);
            addView(banner, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER));
            banner.resume();
            banner.loadAd();
        } else {
            banner.resume();
            scheduleRetry();
        }
    }

    @Override public void onAdLoaded() {
        if (released) return;
        hasAd = true;
        retryAt = 0;
        removeCallbacks(retry);
        update();
        if (BuildConfig.DEBUG) Log.d("IruljidoAdFit", "Bottom banner loaded");
    }

    @Override public void onAdFailed(int code) {
        if (released) return;
        // AdFit keeps a previously loaded creative on refresh failures.
        if (!hasAd) {
            retryAt = SystemClock.uptimeMillis() + RETRY_DELAY_MS;
            scheduleRetry();
        }
        if (BuildConfig.DEBUG) Log.d("IruljidoAdFit", "Bottom banner unavailable: " + code);
    }

    private final Runnable retry = () -> {
        retryAt = 0;
        if (running) banner.loadAd();
    };

    private void scheduleRetry() {
        removeCallbacks(retry);
        if (running && retryAt != 0) {
            postDelayed(retry, Math.max(0L, retryAt - SystemClock.uptimeMillis()));
        }
    }

    void release() {
        released = true;
        running = false;
        removeCallbacks(retry);
        if (banner != null) banner.destroy();
    }
}
