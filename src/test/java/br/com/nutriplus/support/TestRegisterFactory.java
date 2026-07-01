package br.com.nutriplus.support;

public final class TestRegisterFactory {

    private static final String DEFAULT_BIRTH_DATE = "1990-06-15";
    private static final String DEFAULT_CONTACT_PHONE = "11999999999";

    private TestRegisterFactory() {
    }

    public static String body(String name, String email, String password, String cpf) {
        return """
                {"name":"%s","email":"%s","password":"%s","cpf":"%s","birthDate":"%s","contactPhone":"%s"}
                """.formatted(name, email, password, cpf, DEFAULT_BIRTH_DATE, DEFAULT_CONTACT_PHONE);
    }
}
