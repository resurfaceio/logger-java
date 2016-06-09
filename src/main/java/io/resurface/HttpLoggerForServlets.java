// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Servlet filter for HTTP usage logging.
 */
public class HttpLoggerForServlets implements Filter {

    /**
     * Called when filter is placed into service.
     */
    public void init(FilterConfig config) {
        this.config = config;
        this.logger = HttpLoggerFactory.get();
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
        if (logger.isActive()) {
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
                if (request_wrapper == null) {
                    logger.logRequest(request, null);
                } else {
                    logger.logRequest(request, new String(request_wrapper.logged(), request_encoding));
                }
                logger.logResponse(response, new String(response_wrapper.logged(), response_encoding));
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

}
