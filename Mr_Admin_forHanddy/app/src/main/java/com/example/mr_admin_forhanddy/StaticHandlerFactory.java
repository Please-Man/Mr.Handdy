package com.example.mr_admin_forhanddy;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

public class StaticHandlerFactory {

    public static StaticHandler create(IStaticHandler ref) {
        return new StaticHandler(ref);
    }

    // This has to be nested.
    static class StaticHandler extends Handler {
        WeakReference<IStaticHandler> weakRef;

        public StaticHandler(IStaticHandler ref) {
            this.weakRef = new WeakReference<IStaticHandler>(ref);
        }

        @Override
        public void handleMessage(Message msg) {
            if (weakRef.get() == null) {
                throw new RuntimeException("Something goes wrong.");
            } else {
                weakRef.get().handleMessage(msg);
            }
        }
    }
}
