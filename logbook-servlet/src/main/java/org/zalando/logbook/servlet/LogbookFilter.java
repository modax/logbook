package org.zalando.logbook.servlet;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.With;
import org.apiguardian.api.API;
import org.zalando.logbook.HttpRequest;
import org.zalando.logbook.Logbook;
import org.zalando.logbook.Logbook.RequestWritingStage;
import org.zalando.logbook.Logbook.ResponseProcessingStage;
import org.zalando.logbook.Logbook.ResponseWritingStage;
import org.zalando.logbook.Strategy;
import static javax.servlet.DispatcherType.ASYNC;
import static lombok.AccessLevel.PRIVATE;
import static org.apiguardian.api.API.Status.STABLE;

@API(status = STABLE)
@AllArgsConstructor(access = PRIVATE)
public final class LogbookFilter implements HttpFilter {

    /**
     * Unique per instance so we don't accidentally share stages between filter
     * instances in the same chain.
     */
    private final String responseProcessingStageName = ResponseProcessingStage.class.getName() + "-" + UUID.randomUUID();
    private final String responseWritingStageSynchronizationName = ResponseWritingStage.class.getName() + "-Synchronization-"+ UUID.randomUUID();
    private final String responseWritingStageHadErrorName = ResponseWritingStage.class.getName() + "-HasError-" + UUID.randomUUID();

    private final Logbook logbook;
    private final Strategy strategy;

    @With
    private final FormRequestMode formRequestMode;

    public LogbookFilter() {
        this(Logbook.create());
    }

    public LogbookFilter(final Logbook logbook) {
        this(logbook, null);
    }

    public LogbookFilter(final Logbook logbook, @Nullable final Strategy strategy) {
        this(logbook, strategy, FormRequestMode.fromProperties());
    }

    @Override
    public void doFilter(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse,
                         final FilterChain chain) throws ServletException, IOException {

        final RemoteRequest request = new RemoteRequest(httpRequest, formRequestMode);
        final LocalResponse response = new LocalResponse(httpResponse, request.getProtocolVersion());

        final ResponseProcessingStage processing;

        if (request.getDispatcherType() == ASYNC) {
            processing = (ResponseProcessingStage) request.getAttribute(responseProcessingStageName);
        } else {
            processing = process(request).write();
            request.setAttribute(responseProcessingStageName, processing);
        }

        final ResponseWritingStage writing = processing.process(response);
        request.setAsyncListener(Optional.of(new LogbookAsyncListener(
            event -> this.write(request, response, writing),
            event -> request.setAttribute(this.responseWritingStageHadErrorName, Boolean.TRUE)
        )));
        request.setAttribute(responseWritingStageSynchronizationName, new AtomicBoolean(false));
        request.setAttribute(this.responseWritingStageHadErrorName, Boolean.FALSE);

        chain.doFilter(request, response);

        if (request.isAsyncStarted()) {
            return;
        }

        write(request, response, writing);
    }

    private void write(RemoteRequest request, LocalResponse response, ResponseWritingStage writing) throws IOException {
        AtomicBoolean synchronizationAttr = (AtomicBoolean)request.getAttribute(this.responseWritingStageSynchronizationName);
        Boolean hadErrorAttr = (Boolean)request.getAttribute(this.responseWritingStageHadErrorName);
        if (!synchronizationAttr.getAndSet(true)) {
            if (!hadErrorAttr) {
                response.flushBuffer();
            }
            writing.write();
        }
    }

    private RequestWritingStage process(
            final HttpRequest request) throws IOException {

        return strategy == null ?
                logbook.process(request) :
                logbook.process(request, strategy);
    }
}
