// © 2016-2017 Resurface Labs LLC

package io.resurface;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import static io.resurface.HttpLogger.isStringContentType;

/**
 * Servlet filter for HTTP usage logging.
 */
public class HttpLoggerForServlets implements Filter {

    /**
     * Initialize with default parameters.
     */
    public HttpLoggerForServlets() {
        this.queue = null;
    }

    /**
     * Initialize filter using supplied queue.
     */
    public HttpLoggerForServlets(List<String> queue) {
        this.queue = queue;
    }

    /**
     * Called when filter is placed into service.
     */
    public void init(FilterConfig config) {
        this.config = config;
        if (this.queue != null) {
            this.logger = new HttpLogger(queue);
        } else if (config != null) {
            this.logger = new HttpLogger(config.getInitParameter("url"));
        } else {
            this.logger = new HttpLogger();
        }
    }

    /**
     * Called when filter is taken out of service.
     */
    public void destroy() {
        this.config = null;
        this.logger = null;
    }

    /**
     * Called when request/response passes through the filter chain.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (logger.isEnabled()) {
            process((HttpServletRequest) request, (HttpServletResponse) response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * Called when an active logger passes a request/response through the filter chain.
     */
    protected void process(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Construct request and response wrappers
        LoggedRequestWrapper request_wrapper = null;
        if (isStringContentType(request.getContentType())) request_wrapper = new LoggedRequestWrapper(request);
        LoggedResponseWrapper response_wrapper = new LoggedResponseWrapper(response);

        // Pass request & response wrappers through filter chain
        chain.doFilter(request_wrapper != null ? request_wrapper : request, response_wrapper);
        response_wrapper.flushBuffer();

        // Log successful responses having string content types
        if ((response.getStatus() < 300) && isStringContentType(response.getContentType())) {
            String request_encoding = request.getCharacterEncoding();
            request_encoding = (request_encoding == null) ? "ISO-8859-1" : request_encoding;
            String response_encoding = response.getCharacterEncoding();
            response_encoding = (response_encoding == null) ? "ISO-8859-1" : response_encoding;
            String request_body = request_wrapper == null ? null : new String(request_wrapper.logged(), request_encoding);
            String response_body = new String(response_wrapper.logged(), response_encoding);
            logger.log(request, request_body, response, response_body);
        }
    }

    protected FilterConfig config;
    protected HttpLogger logger;
    protected final List<String> queue;

}
