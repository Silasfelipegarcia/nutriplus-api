package br.com.nutriplus.domain.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class LanguageCodec {

    private LanguageCodec() {
    }

    public static String encode(List<String> languages) {
        if (languages == null || languages.isEmpty()) {
            return null;
        }
        String encoded = languages.stream()
                .map(s -> s == null ? "" : s.trim().toUpperCase())
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.joining(","));
        return encoded.isBlank() ? null : encoded;
    }

    public static List<String> decode(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
