package ru.netology;


import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {


    private final List<String> validPaths = List.of(
            "/index.html",
            "/spring.svg",
            "/spring.png",
            "/resources.html",
            "/styles.css",
            "/app.js",
            "/links.html",
            "/forms.html",
            "/classic.html",
            "/events.html",
            "/events.js",
            "/default_get.html"
    );

    public Server(int port, int threadPoolSize) {
        executor = Executors.newFixedThreadPool(threadPoolSize);
        this.port = port;
    }

    private final ExecutorService executor;
    private final int port;

    public void start() throws IOException {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                executor.execute(handleRequest(serverSocket.accept()));
            }
        }
    }

    private Runnable handleRequest(Socket socket) {
        return (() -> {
            try (final var in = new BufferedInputStream(socket.getInputStream());
                 final var out = new BufferedOutputStream(socket.getOutputStream())) {



                Request request = new Request(in);


//                for (var pair : request.getQueryParams()) {
//                    System.out.println(pair.getName() + " = " + pair.getValue());
//                }
//                System.out.println(request.getQueryParam("last"));
//                System.out.println(request.getHeaderParam("Content-Type"));
//
//                for (var pair : request.getPostParams()) {
//                    System.out.println(pair.getName() + " = " + pair.getValue());
//                }
//                System.out.println(request.getPostParam("value"));

                if (request.getMethod().equals("POST")) {
                    Map<String, List<FileItem>> map = request.getParts();
                    for (Map.Entry<String, List<FileItem>> entry : map.entrySet()) {
                        String paramName = entry.getKey();
                        List<FileItem> values = entry.getValue();

                        for (FileItem item : values) {
                            if (item.isFormField()) {
                                System.out.println(paramName + " = " + item.getString());
                            } else {
                                System.out.println(paramName + " (файл) = " + item.getName() + ", размер: " + item.getSize());
                            }
                        }
                    }

                    for (FileItem item : request.getPart("image")) {
                        if (item.isFormField()) {
                            System.out.println(item.getFieldName() + " = " + item.getString());
                        } else {
                            System.out.println(item.getFieldName() + " (файл) = " + item.getName() + ", размер: " + item.getSize());
                        }

                    }
                }


                if (!validPaths.contains(request.getPath())) {
                    writeResponse(out, "404 Not Found", "text/plain", new byte[0]);
                    return;
                }

                final var filePath = Path.of(".", "public", request.getPath());
                final var mimeType = Files.probeContentType(filePath);

                // special case for classic
                if (request.getPath().equals("/classic.html")) {
                    handleClassicHtml(out, filePath, mimeType);
                    return;
                }

                handlePath(out, filePath, mimeType);

            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void handleClassicHtml(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        final var template = Files.readString(filePath);
        final var content = template.replace(
                "{time}",
                LocalDateTime.now().toString()
        ).getBytes();
        writeResponse(out, "200 OK", mimeType, content);
    }

    private void handlePath(BufferedOutputStream out, Path filePath, String mimeType) throws IOException {
        final var length = Files.size(filePath);
        writeHeader(out, "200 OK", mimeType, length);
        Files.copy(filePath, out);
        out.flush();
    }

    private void writeHeader(OutputStream out, String status, String mimeType, long length) throws IOException {
        out.write((
                "HTTP/1.1 " + status + "\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
    }

    private void writeResponse(OutputStream out, String status, String mimeType, byte[] content) throws IOException {
        writeHeader(out, status, mimeType, content.length);
        out.write(content);
        out.flush();
    }

}
