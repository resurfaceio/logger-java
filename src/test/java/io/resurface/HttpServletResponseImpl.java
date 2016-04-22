// Copyright (c) 2016 Resurface Labs LLC, All Rights Reserved

package io.resurface;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

/**
 * HttpServletResponse implementation for custom usage logging.
 */
public class HttpServletResponseImpl implements HttpServletResponse {

    public HttpServletResponseImpl() {
        servletOutputStream = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                // do nothing
            }
        };
    }

    @Override
    public void addCookie(Cookie cookie) {

    }

    @Override
    public void addDateHeader(String name, long date) {

    }

    @Override
    public void addHeader(String name, String value) {

    }

    @Override
    public void addIntHeader(String name, int value) {

    }

    @Override
    public boolean containsHeader(String name) {
        return false;
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return null;  // deprecated
    }

    @Override
    public String encodeRedirectURL(String url) {
        return null;
    }

    @Override
    public String encodeUrl(String url) {
        // deprecated
        return null;
    }

    @Override
    public String encodeURL(String url) {
        return null;
    }

    @Override
    public void flushBuffer() throws IOException {

    }

    @Override
    public int getBufferSize() {
        return 0;
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return null;
    }

    @Override
    public Collection<String> getHeaderNames() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return servletOutputStream;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(getOutputStream());
    }

    @Override
    public boolean isCommitted() {
        return false;
    }

    @Override
    public void reset() {

    }

    @Override
    public void resetBuffer() {

    }

    @Override
    public void sendError(int sc) throws IOException {

    }

    @Override
    public void sendError(int sc, String msg) throws IOException {

    }

    @Override
    public void sendRedirect(String location) throws IOException {

    }

    @Override
    public void setBufferSize(int size) {

    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }

    @Override
    public void setContentLength(int len) {

    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public void setDateHeader(String name, long date) {

    }

    @Override
    public void setHeader(String name, String value) {

    }

    @Override
    public void setIntHeader(String name, int value) {

    }

    @Override
    public void setLocale(Locale loc) {

    }

    @Override
    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public void setStatus(int status, String sm) {
        this.status = status;
    }

    private final ServletOutputStream servletOutputStream;
    private String characterEncoding;
    private String contentType;
    private int status;
}
