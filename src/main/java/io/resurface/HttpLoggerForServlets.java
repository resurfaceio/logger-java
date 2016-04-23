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

    private FilterConfig config;
    private HttpLogger logger;

    /**
     * Called when request/response passes through the filter chain.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        if (!logger.isActive()) {
            chain.doFilter(req, res);
        } else {
            HttpServletRequest request = (HttpServletRequest) req;
            HttpServletResponse response = (HttpServletResponse) res;
            LoggedResponseWrapper wrapper = new LoggedResponseWrapper(response);
            chain.doFilter(request, wrapper);
            if (response.getStatus() != 304) {
                String content_type = response.getContentType();
                String encoding = response.getCharacterEncoding();
                if ((content_type != null) && (encoding != null)) {
                    boolean is_html = content_type.startsWith("text/html");
                    boolean is_json = content_type.startsWith("application/json");
                    boolean is_soap = content_type.startsWith("application/soap+xml");
                    boolean is_xml1 = content_type.startsWith("application/xml");
                    boolean is_xml2 = content_type.startsWith("text/xml");
                    if (is_html || is_json || is_soap || is_xml1 || is_xml2) {
                        logger.logRequest(request);
                        logger.logResponse(response, new String(wrapper.read(), encoding));
                    }
                }
            }
        }
    }

}
