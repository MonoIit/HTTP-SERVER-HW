package ru.netology;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipartParser {

    public static Map<String, List<FileItem>> parseBody(String body, String contentType, int contentLength) throws Exception {

        System.out.println(System.getProperty("tmpdir"));
        DiskFileItemFactory factory = new DiskFileItemFactory();
        FileUpload upload = new FileUpload(factory);
        upload.setHeaderEncoding("UTF-8");

        ByteArrayInputStream input = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

        RequestContext context = new RequestContext() {
            @Override
            public String getCharacterEncoding() { return "UTF-8"; }
            @Override
            public String getContentType() { return contentType; }
            @Override
            public int getContentLength() { return contentLength; }
            @Override
            public InputStream getInputStream() throws IOException { return input; }
        };

        Map<String, List<FileItem>> paramsMap = new HashMap<>();
        List<FileItem> items = upload.parseRequest(context);
        for (FileItem item : items) {
            paramsMap.computeIfAbsent(item.getFieldName(), k -> new ArrayList<>()).add(item);

            if (!item.isFormField()) {
                // item.write(new File("tmpdir/" + item.getName()));
            }
        }

        return paramsMap;
    }
}
