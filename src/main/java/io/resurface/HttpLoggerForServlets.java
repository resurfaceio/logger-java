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
    }

    private FilterConfig config;

    /**
     * Called when filter is taken out of service.
     */
    public void destroy() {
        this.config = null;
    }

    /**
     * Called when request/response passes through the filter chain.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpLogger logger = HttpLoggerFactory.get();
        logger.logRequest(request);
        LoggedResponseWrapper wrapper = new LoggedResponseWrapper(response);
        chain.doFilter(request, wrapper);

        String content_type = response.getContentType();
        String encoding = response.getCharacterEncoding();
        String url = request.getRequestURL().toString();
        String fake_body = "request.getRequestURL()=" + url + " | response.getContentType()=" + content_type + " | response.getCharacterEncoding()=" + encoding + " | ";
        boolean is_text = (encoding != null) && (content_type != null) && content_type.toLowerCase().startsWith("text/") && (response.getStatus() != 304);
        if (is_text) {
            fake_body += new String(wrapper.read(), encoding);
        } else {
            fake_body += "IS_NOT_TEXT";
        }

        logger.logResponse(response, fake_body);
    }

}
