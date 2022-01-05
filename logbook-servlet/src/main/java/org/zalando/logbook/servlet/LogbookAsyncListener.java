package org.zalando.logbook.servlet;

import javax.annotation.Nullable;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import java.io.IOException;

class LogbookAsyncListener implements AsyncListener {

    private final AsyncOnEventListener onCompleteListener;
    private final AsyncOnEventListener onErrorListener;

    public LogbookAsyncListener(
        @Nullable AsyncOnEventListener onCompleteListener,
        @Nullable AsyncOnEventListener onErrorListener
    ) {
        this.onCompleteListener = onCompleteListener;
        this.onErrorListener = onErrorListener;
    }

    @Override
    public void onComplete(AsyncEvent event) throws IOException {
        if (onCompleteListener != null) {
            onCompleteListener.onEvent(event);
        }
    }

    @Override
    public void onTimeout(AsyncEvent event) {
    }

    @Override
    public void onError(AsyncEvent event) throws IOException {
        if (onErrorListener != null) {
            onErrorListener.onEvent(event);
        }
    }

    @Override
    public void onStartAsync(AsyncEvent event) {
    }
}
