package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.naming.Name;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Request {
    public static final List<String> ALLOWED_METHODS = List.of("GET", "POST");
    public static final byte[] REQUEST_LINE_DELIMITER = new byte[]{'\r', '\n'};
    public static final byte[] HEADERS_DELIMITER = new byte[]{'\r', '\n', '\r', '\n'};
    public static final int REQUEST_LINE_LENGTH = 3;
    public static final int BUFFER_LIMIT = 4096;

    private String method;
    private String path;
    private Map<String, String> headers;
    private Map<String, List<String>> postParams;
    private String body;

    public Request(BufferedInputStream in) throws IOException {
        in.mark(BUFFER_LIMIT);
        final var buffer = new byte[BUFFER_LIMIT];
        final var read = in.read(buffer);

        // ищем request line
        final var requestLineEnd = indexOf(buffer, REQUEST_LINE_DELIMITER, 0, read);
        if (requestLineEnd == -1) {
            throw new IOException("bad request");
        }

        // читаем request line
        final var requestLine = new String(Arrays.copyOf(buffer, requestLineEnd)).split(" ");
        if (requestLine.length != REQUEST_LINE_LENGTH) {
            throw new IOException("bad request");
        }

        method = requestLine[0];
        if (!ALLOWED_METHODS.contains(method)) {
            throw new IOException("bad request");
        }
        System.out.println(method);

        path = requestLine[1];
        if (!path.startsWith("/")) {
            throw new IOException("bad request");
        }
        System.out.println(path);

        // ищем заголовки
        final var headersStart = requestLineEnd + HEADERS_DELIMITER.length;
        final var headersEnd = indexOf(buffer, HEADERS_DELIMITER, headersStart, read);
        if (headersEnd == -1) {
            throw new IOException("bad request");
        }
        final var listOfHeaders = new String(getNBytes(buffer, headersStart, headersEnd - headersStart)).split("\r\n");
        headers = new HashMap<>();
        for (String str : listOfHeaders) {
            int seporatorIdx = str.indexOf(':');
            if (seporatorIdx != -1) {
                String key = str.substring(0, seporatorIdx).trim();
                String value = str.substring(seporatorIdx + 1).trim();
                headers.put(key, value);
            }
        }
        System.out.println(headers);

        // отматываем на начало буфера
        in.reset();
        // пропускаем requestLine
        in.skip(headersEnd);

        if (!method.equals("GET")) {
            in.skip(Request.HEADERS_DELIMITER.length);
            // вычитываем Content-Length, чтобы прочитать body
            final var contentLength = getHeaderParam("Content-Length");
            if (contentLength != null) {
                final var length = Integer.parseInt(contentLength);
                final var bodyBytes = in.readNBytes(length);

                body = new String(bodyBytes);
                System.out.println(body);
            }
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String uri) {
        this.path = uri;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public String getHeaderParam(String key) {
        return headers.getOrDefault(key, null);
    }


    public Map<String, List<String>> getPostParams() {
        if (postParams == null) {
            postParams = urlencodedParser.parse(body);
        }
        return postParams;
    }

    public List<String> getPostParam(String name) {
        return postParams.getOrDefault(name, null);
    }

    private int indexOf(byte[] array, byte[] target, int start, int max) {
        outer:
        for (int i = start; i < max - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) {
                    continue outer;
                }
            }
            return i;
        }
        return -1;
    }
    
    private byte[] getNBytes(byte[] array, int start, int size) {
        byte[] subArray = new byte[size];
        System.arraycopy(array, start, subArray, 0, start + size - start);

        return subArray;
    }
}
