package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private static final String REQUEST_LINE_SEPORATOR = " ";
    private static final int REQUEST_LINE_LENGTH = 3;

    private String method;
    private String url;
    private Map<String, String> Headers;
    private List<NameValuePair> queryParams;
    private String body;

    public Request(String requestLine) {
        final var parts = requestLine.split(REQUEST_LINE_SEPORATOR);
        if (parts.length != REQUEST_LINE_LENGTH) throw new RuntimeException("bad requestLine");
        method = parts[0];
        url = parts[1];

        String query = url.substring(url.indexOf('?') + 1);
        queryParams = URLEncodedUtils.parse(query, StandardCharsets.UTF_8);
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String uri) {
        this.url = uri;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }


    public List<NameValuePair> getQueryParams() {
        return queryParams;
    }

    public String getQueryParam(String name) {
        for (var pair : queryParams) {
            if (pair.getName().equals(name)) return pair.getValue();
        }
        return null;
    }
}
