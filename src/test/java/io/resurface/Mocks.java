package io.resurface;

import org.mockito.Mockito;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.mockito.Mockito.mock;

/**
 * Provides mock objects used for testing.
 */
public class Mocks {

    public static String[] MOCK_INVALID_URLS = {HttpLogger.URL + "/noway3is5this1valid2", "https://www.noway3is5this1valid2.com/", "http://www.noway3is5this1valid2.com/"};

    public static HttpServletRequest mockRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Mockito.when(request.getRequestURL()).thenReturn(new StringBuffer("http://something.com/index.html"));
        return request;
    }

    public static HttpServletResponse mockResponse() throws IOException {
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        HttpServletResponse response = mock(HttpServletResponse.class);
        Mockito.when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                os.write(b);
            }
        });
        Mockito.when(response.getStatus()).thenReturn(201);
        return response;
    }

}
