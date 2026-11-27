package momo.kikiplus.com.kbucket;

import android.content.Intent;

/** Widget launches have their own task so an open dream editor keeps its place. */
public class MemoWindow extends Home {
    @Override boolean memoWindow() { return true; }

    @Override synchronized void acceptIntent(Intent intent) {
        inbox = MemoWidget.target(intent);
        consumedIntent = false;
        if (unlocked) js("window.externalInput && externalInput()");
    }
}
