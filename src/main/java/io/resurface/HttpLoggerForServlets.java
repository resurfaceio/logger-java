// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

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
        throws IOException, ServletException
    {
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
        throws IOException, ServletException
    {
        // Construct request wrapper for string content types
        LoggedRequestWrapper request_wrapper = null;
        String request_encoding = request.getCharacterEncoding();
        if ((request_encoding != null) && isStringContentType(request.getContentType())) {
            request_wrapper = new LoggedRequestWrapper(request);
        }

        // Pass request & response wrappers through filter chain
        LoggedResponseWrapper response_wrapper = new LoggedResponseWrapper(response);
        chain.doFilter(request_wrapper != null ? request_wrapper : request, response_wrapper);
        response_wrapper.flushBuffer();
        if (response.getStatus() != 304) {
            String response_encoding = response.getCharacterEncoding();
            if ((response_encoding != null) && isStringContentType(response.getContentType())) {
                String request_body = request_wrapper == null ? null : new String(request_wrapper.logged(), request_encoding);
                String response_body = new String(response_wrapper.logged(), response_encoding);
                logger.log(request, request_body, response, response_body);
            }
        }
    }

    /**
     * Returns true if content type indicates string data.
     */
    protected boolean isStringContentType(String s) {
        return s != null && (s.startsWith("text/html") || s.startsWith("text/plain") || s.startsWith("text/xml")
            || s.startsWith("application/json") || s.startsWith("application/soap+xml")
            || s.startsWith("application/xml") || s.startsWith("application/x-www-form-urlencoded"));
    }

    protected FilterConfig config;
    protected HttpLogger logger;
    protected final List<String> queue;

}
