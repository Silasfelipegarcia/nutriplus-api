package br.com.nutriplus.util;

import br.com.nutriplus.exception.BusinessException;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public final class TimeInputNormalizer {

    private static final Pattern FLEXIBLE_TIME = Pattern.compile("^(\\d{1,2}):(\\d{1,2})$");
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private TimeInputNormalizer() {
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new BusinessException("Informe o horário no formato HH:mm (ex.: 07:00).");
        }
        var matcher = FLEXIBLE_TIME.matcher(raw.trim());
        if (!matcher.matches()) {
            throw new BusinessException("Horário inválido. Use HH:mm (ex.: 07:00 ou 6:00).");
        }
        int hour = Integer.parseInt(matcher.group(1));
        int minute = Integer.parseInt(matcher.group(2));
        if (hour < 0 || hour > 23 || minute < 0 || minute > 59) {
            throw new BusinessException("Horário fora do intervalo válido (00:00–23:59).");
        }
        return String.format("%02d:%02d", hour, minute);
    }

    public static LocalTime parseFlexible(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return LocalTime.parse(normalize(raw), HH_MM);
    }
}
