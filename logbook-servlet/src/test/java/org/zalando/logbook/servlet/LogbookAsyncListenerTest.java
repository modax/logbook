package org.zalando.logbook.servlet;

import java.io.IOException;
import javax.servlet.AsyncEvent;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LogbookAsyncListenerTest {

    private final AsyncOnEventListener asyncOnCompleteListener = mock(AsyncOnEventListener.class);
    private final AsyncOnEventListener asyncOnErrorListener = mock(AsyncOnEventListener.class);
    private final AsyncEvent asyncEvent = mock(AsyncEvent.class);

    private final LogbookAsyncListener logbookAsyncListener = new LogbookAsyncListener(asyncOnCompleteListener, asyncOnErrorListener);

    @Test
    void onComplete() throws IOException {
        logbookAsyncListener.onComplete(asyncEvent);

        verify(asyncOnCompleteListener).onEvent(asyncEvent);
    }

    @Test
    void onTimeout() {
        logbookAsyncListener.onTimeout(asyncEvent);
    }

    @Test
    void onError() throws IOException {
        logbookAsyncListener.onError(asyncEvent);

        verify(asyncOnErrorListener).onEvent(asyncEvent);
    }

    @Test
    void onStartAsync() {
        logbookAsyncListener.onStartAsync(asyncEvent);
    }
}