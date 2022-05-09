// © 2016-2022 Resurface Labs Inc.

package io.resurface;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Provider
public class HttpLoggerForJersey implements ContainerRequestFilter, ContainerResponseFilter, ReaderInterceptor, WriterInterceptor {

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
     * Interceptor method called when request is first accepted.
     */
    @Override
    public void filter(ContainerRequestContext context) {
        context.setProperty("resurfaceio.start", System.nanoTime());
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
        List<String[]> message = new ArrayList<>();
        String method = request.getMethod();
        if (method != null) message.add(new String[]{"request_method", method});
        String formatted_url = request.getUriInfo().getRequestUri().toString();
        if (formatted_url != null) message.add(new String[]{"request_url", formatted_url});
        message.add(new String[]{"response_code", String.valueOf(response.getStatus())});
        appendRequestHeaders(message, request);
        appendRequestParams(message, request);
        appendResponseHeaders(message, response);
        request.setProperty("resurfaceio.message", message);
    }

    /**
     * Adds request headers to message.
     */
    private static void appendRequestHeaders(List<String[]> message, ContainerRequestContext request) {
        for (Map.Entry<String, List<String>> x : request.getHeaders().entrySet()) {
            String name = "request_header:" + x.getKey().toLowerCase();
            for (String xv : x.getValue()) message.add(new String[]{name, xv});
        }
    }

    /**
     * Adds request params to message.
     */
    private static void appendRequestParams(List<String[]> message, ContainerRequestContext request) {
        for (Map.Entry<String, List<String>> x : request.getUriInfo().getQueryParameters(true).entrySet()) {
            String name = "request_param:" + x.getKey().toLowerCase();
            for (String xv : x.getValue()) message.add(new String[]{name, xv});
        }
    }

    /**
     * Adds response headers to message.
     */
    private static void appendResponseHeaders(List<String[]> message, ContainerResponseContext response) {
        for (Map.Entry<String, List<String>> x : response.getStringHeaders().entrySet()) {
            String name = "response_header:" + x.getKey().toLowerCase();
            for (String xv : x.getValue()) message.add(new String[]{name, xv});
        }
    }

    /**
     * Interceptor method called when writing out the response body.
     */
    @Override
    public void aroundWriteTo(WriterInterceptorContext context) throws IOException, WebApplicationException {
        if (logger.enabled) {
            List<String[]> message = (List<String[]>) context.getProperty("resurfaceio.message");
            LoggedOutputStream los = new LoggedOutputStream(context.getOutputStream());
            context.setOutputStream(los);
            context.proceed();
            byte[] rbb = (byte[]) context.getProperty("resurfaceio.requestBodyBytes");
            String request_body = (rbb == null) ? null : new String(rbb, StandardCharsets.UTF_8);
            if (request_body != null && !request_body.equals("")) message.add(new String[]{"request_body", request_body});
            String response_body = new String(los.logged(), StandardCharsets.UTF_8);
            if (!response_body.equals("")) message.add(new String[]{"response_body", response_body});
            message.add(new String[]{"now", String.valueOf(System.currentTimeMillis())});
            double interval = (System.nanoTime() - (Long) context.getProperty("resurfaceio.start")) / 1000000.0;
            message.add(new String[]{"interval", String.valueOf(interval)});
            logger.submitIfPassing(message);
        } else {
            context.proceed();
        }
    }

    protected final HttpLogger logger;

}
