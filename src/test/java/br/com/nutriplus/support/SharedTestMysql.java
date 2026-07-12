package br.com.nutriplus.support;

import org.testcontainers.containers.MySQLContainer;

/**
 * MySQL único para todos os testes de integração no mesmo JVM (inicialização lazy).
 */
public final class SharedTestMysql {

    private static volatile MySQLContainer<?> instance;

    private SharedTestMysql() {
    }

    public static MySQLContainer<?> get() {
        if (instance == null) {
            synchronized (SharedTestMysql.class) {
                if (instance == null) {
                    instance = new MySQLContainer<>("mysql:8.4")
                            .withDatabaseName("nutriplus_test")
                            .withUsername("test")
                            .withPassword("test");
                    instance.start();
                }
            }
        }
        return instance;
    }
}
