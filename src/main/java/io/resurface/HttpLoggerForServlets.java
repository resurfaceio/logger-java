// © 2016-2019 Resurface Labs Inc.

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
        this.rules = null;
    }

    /**
     * Initialize filter using supplied queue.
     */
    public HttpLoggerForServlets(List<String> queue, String rules) {
        this.queue = queue;
        this.rules = rules;
    }

    /**
     * Returns wrapped logger instance.
     */
    public HttpLogger getLogger() {
        return this.logger;
    }

    /**
     * Called when filter is placed into service.
     */
    public void init(FilterConfig config) {
        this.config = config;
        if (this.queue != null) {
            this.logger = new HttpLogger(queue, rules);
        } else if (config != null) {
            this.logger = new HttpLogger(config.getInitParameter("url"), config.getInitParameter("rules"));
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
            log((HttpServletRequest) request, (HttpServletResponse) response, chain);
        } else {
            chain.doFilter(request, response);
        }
    }

    /**
     * Logs the request/response from within the filter chain.
     */
    protected void log(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // Construct response wrapper and pass through filter chain
        LoggedResponseWrapper response_wrapper = new LoggedResponseWrapper(response);
        chain.doFilter(request, response_wrapper);
        response_wrapper.flushBuffer();

        // Log successful responses having string content types
        int status = response.getStatus();
        if ((status < 300 || status == 302) && isStringContentType(response.getContentType())) {
            String response_encoding = response.getCharacterEncoding();
            response_encoding = (response_encoding == null) ? "ISO-8859-1" : response_encoding;
            String response_body = new String(response_wrapper.logged(), response_encoding);
            HttpMessage.send(logger, request, response, response_body);
        }
    }

    protected FilterConfig config;
    protected HttpLogger logger;
    protected final List<String> queue;
    protected final String rules;

}
