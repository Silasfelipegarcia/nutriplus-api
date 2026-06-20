package br.com.nutriplus.domain.util;

import br.com.nutriplus.domain.enums.ServiceMode;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public final class ServiceModeCodec {

    private ServiceModeCodec() {
    }

    public static Set<ServiceMode> decode(String raw) {
        if (raw == null || raw.isBlank()) {
            return EnumSet.of(ServiceMode.ONLINE, ServiceMode.IN_PERSON);
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(ServiceMode::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ServiceMode.class)));
    }

    public static String encode(Set<ServiceMode> modes) {
        if (modes == null || modes.isEmpty()) {
            throw new IllegalArgumentException("Pelo menos um modo de atendimento é obrigatório.");
        }
        return modes.stream().map(Enum::name).sorted().collect(Collectors.joining(","));
    }

    public static boolean includes(String raw, ServiceMode mode) {
        return decode(raw).contains(mode);
    }
}
