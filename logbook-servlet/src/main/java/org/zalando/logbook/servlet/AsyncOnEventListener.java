package org.zalando.logbook.servlet;

import java.io.IOException;
import javax.servlet.AsyncEvent;

@FunctionalInterface
interface AsyncOnEventListener {
    void onEvent(AsyncEvent event) throws IOException;
}
