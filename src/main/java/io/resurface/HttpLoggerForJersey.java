// © 2016-2018 Resurface Labs LLC

package io.resurface;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Provider
public class HttpLoggerForJersey implements ContainerResponseFilter, ReaderInterceptor, WriterInterceptor {

    /**
     * Initialize logger using specified url and default rules.
     */
    public HttpLoggerForJersey(String url) {
        logger = new HttpLogger(url);
    }

    /**
     * Initialize logger using specified url and specified rules.
     */
    public HttpLoggerForJersey(String url, String rules) {
        logger = new HttpLogger(url, rules);
    }

    /**
     * Initialize enabled logger using queue and default rules.
     */
    public HttpLoggerForJersey(List<String> queue) {
        logger = new HttpLogger(queue);
    }

    /**
     * Initialize enabled logger using queue and specified rules.
     */
    public HttpLoggerForJersey(List<String> queue, String rules) {
        logger = new HttpLogger(queue, rules);
    }

    /**
     * Returns wrapped logger instance.
     */
    public HttpLogger getLogger() {
        return this.logger;
    }

    /**
     * Interceptor method called when reading from the request body.
     */
    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        if (logger.enabled) {
            LoggedInputStream lis = new LoggedInputStream(context.getInputStream());
            context.setProperty("resurfaceio.requestBodyBytes", lis.logged());
            context.setInputStream(lis);
        }
        return context.proceed();
    }

    /**
     * Filter method called after a response has been provided for a request.
     */
    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        if (!logger.enabled) return;

        // Save request details
        HttpServletRequestImpl requestImpl = new HttpServletRequestImpl();
        requestImpl.setMethod(request.getMethod());
        requestImpl.setRequestURL(request.getUriInfo().getRequestUri().toString());
        for (Map.Entry<String, List<String>> x : request.getHeaders().entrySet())
            for (String xv : x.getValue()) requestImpl.addHeader(x.getKey(), xv);
        for (Map.Entry<String, List<String>> x : request.getUriInfo().getQueryParameters(true).entrySet())
            for (String xv : x.getValue()) requestImpl.addParam(x.getKey(), xv);
        request.setProperty("resurfaceio.request", requestImpl);

        // Save response details
        HttpServletResponseImpl responseImpl = new HttpServletResponseImpl();
        responseImpl.setStatus(response.getStatus());
        for (Map.Entry<String, List<String>> x : response.getStringHeaders().entrySet())
            for (String xv : x.getValue()) responseImpl.addHeader(x.getKey(), xv);
        request.setProperty("resurfaceio.response", responseImpl);
    }

    /**
     * Interceptor method called when writing out the response body.
     */
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        HttpServletResponseImpl response = (HttpServletResponseImpl) context.getProperty("resurfaceio.response");
        if (logger.enabled && (response.getStatus() < 300 || response.getStatus() == 302)) {
            LoggedOutputStream los = new LoggedOutputStream(context.getOutputStream());
            context.setOutputStream(los);
            context.proceed();
            String responseBody = new String(los.logged(), StandardCharsets.UTF_8);
            byte[] rbb = (byte[]) context.getProperty("resurfaceio.requestBodyBytes");
            String requestBody = (rbb == null) ? null : new String(rbb, StandardCharsets.UTF_8);
            HttpServletRequestImpl request = (HttpServletRequestImpl) context.getProperty("resurfaceio.request");
            logger.submit(logger.format(request, response, responseBody, requestBody));
        } else {
            context.proceed();
        }
    }

    protected final HttpLogger logger;

}
