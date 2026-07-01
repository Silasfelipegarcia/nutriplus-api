package br.com.nutriplus.support;

public final class JsonTestSupport {

    private JsonTestSupport() {
    }

    public static String extractStringField(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalStateException("Field not found: " + field);
        }
        start += marker.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }

    public static String extractNestedStringField(String json, String objectField, String field) {
        String objectMarker = "\"" + objectField + "\":{";
        int objectStart = json.indexOf(objectMarker);
        if (objectStart < 0) {
            throw new IllegalStateException("Object not found: " + objectField);
        }
        return extractStringField(json.substring(objectStart), field);
    }

    public static long extractNestedNumberField(String json, String objectField, String field) {
        String objectMarker = "\"" + objectField + "\":{";
        int objectStart = json.indexOf(objectMarker);
        if (objectStart < 0) {
            throw new IllegalStateException("Object not found: " + objectField);
        }
        String slice = json.substring(objectStart);
        String marker = "\"" + field + "\":";
        int start = slice.indexOf(marker);
        if (start < 0) {
            throw new IllegalArgumentException("Field not found: " + field);
        }
        start += marker.length();
        int end = start;
        while (end < slice.length() && Character.isDigit(slice.charAt(end))) {
            end++;
        }
        return Long.parseLong(slice.substring(start, end));
    }
}
