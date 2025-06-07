package ru.netology;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class urlencodedParser {

    public static Map<String, List<String>> parse(String body) {
        List<NameValuePair> params = URLEncodedUtils.parse(body, StandardCharsets.UTF_8);
        Map<String, List<String>> paramsMap = new HashMap<>();
        for (NameValuePair pair : params) {
            paramsMap.computeIfAbsent(pair.getName(), k -> new ArrayList<>()).add(pair.getValue());
        }
        return paramsMap;
    }
}
