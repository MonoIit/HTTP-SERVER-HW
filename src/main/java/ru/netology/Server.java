package ru.netology;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
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
            "/events.js"
    );

    public Server(int port, int threadPoolSize) {
        executor = Executors.newFixedThreadPool(64);
        this.port = port;
    }

    private final ExecutorService executor;
    private final int port;

    public void start() throws IOException {
        try (final var serverSocket = new ServerSocket(9999)) {
            while (true) {
                executor.execute(handleRequest(serverSocket.accept()));
            }
        }
    }

    private Runnable handleRequest(Socket socket) {
        return (() -> {
            try (final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 final var out = new BufferedOutputStream(socket.getOutputStream())) {

                    final var requestLine = in.readLine();
                    final var parts = requestLine.split(" ");

                    if (parts.length != 3) return;

                    final var path = parts[1];
                    if (!validPaths.contains(path)) {
                        writeResponse(out, "404 Not Found", "text/plain", new byte[0]);
                        return;
                    }

                    final var filePath = Path.of(".", "public", path);
                    final var mimeType = Files.probeContentType(filePath);

                    // special case for classic
                    if (path.equals("/classic.html")) {
                        handleClassicHtml(out, filePath, mimeType);
                        return;
                    }

                    handlePath(out, filePath, mimeType);

            } catch (IOException e) {
                e.printStackTrace();
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
