package bbejeck.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.DispatcherServlet;

@WebServlet(urlPatterns = {"/async/*"}, asyncSupported = true, name = "async")

public class AsyncDispatcherServlet extends DispatcherServlet {

    private ExecutorService exececutor;
    private static final int NUM_ASYNC_TASKS = 15;
    private static final long TIME_OUT = 10 * 1000;
    private final Log log = LogFactory.getLog(AsyncDispatcherServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        exececutor = Executors.newFixedThreadPool(NUM_ASYNC_TASKS);
    }

    @Override
    public void destroy() {
        exececutor.shutdownNow();
        super.destroy();
    }

    @Override
    protected void doDispatch(final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        final AsyncContext ac = request.startAsync(request, response);
        ac.setTimeout(TIME_OUT);
        FutureTask task = new FutureTask(new Runnable() {

            @Override
            public void run() {
                try {
                    log.debug("Dispatching request " + request);
                    AsyncDispatcherServlet.super.doDispatch(request, response);
                    log.debug("doDispatch returned from processing request " + request);
                    ac.complete();
                } catch (Exception ex) {
                    log.error("Error in async request", ex);
                }
            }
        }, null);

        ac.addListener(new AsyncDispatcherServletListener(task));
        exececutor.execute(task);
    }

    private class AsyncDispatcherServletListener implements AsyncListener {

        private FutureTask future;

        public AsyncDispatcherServletListener(FutureTask future) {
            this.future = future;
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            log.warn("Async request did not complete timeout occured");
            handleTimeoutOrError(event, "Request timed out");
        }

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            log.debug("Completed async request");
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
            String error = (event.getThrowable() == null ? "UNKNOWN ERROR" : event.getThrowable().getMessage());
            log.error("Error in async request " + error);
            handleTimeoutOrError(event, "Error processing " + error);
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            log.debug("Async Event started..");
        }

        private void handleTimeoutOrError(AsyncEvent event, String message) {
            PrintWriter writer = null;
            try {
                future.cancel(true);
                HttpServletResponse response = (HttpServletResponse) event.getAsyncContext().getResponse();
                //HttpServletRequest request = (HttpServletRequest) event.getAsyncContext().getRequest();
                //request.getRequestDispatcher("/app/error.htm").forward(request, response);
                writer = response.getWriter();
                writer.print(message);
                writer.flush();
            } catch (Exception ex) {
                log.error(ex);
            } finally {
                event.getAsyncContext().complete();
                if (writer != null) {
                    writer.close();
                }
            }
        }
    }
}
