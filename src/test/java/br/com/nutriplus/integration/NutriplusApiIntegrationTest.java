package br.com.nutriplus.integration;

import br.com.nutriplus.AbstractIntegrationTest;
import br.com.nutriplus.support.TestCpfFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class NutriplusApiIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthReturnsUp() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("nutriplus-backend"));
    }

    @Test
    void registerLoginAndGetMe() throws Exception {
        String email = "user-" + UUID.randomUUID() + "@nutriplus.test";
        String password = "secret123";

        String registerBody = """
                {"name":"Integration User","email":"%s","password":"%s","cpf":"%s"}
                """.formatted(email, password, TestCpfFactory.nextValidCpf());

        String authJson = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andExpect(jsonPath("$.user.email").value(email))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = extractJsonField(authJson, "token");

        mockMvc.perform(get("/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));

        String loginBody = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(notNullValue()));
    }

    @Test
    void updateProfileWithAuth() throws Exception {
        String email = "profile-" + UUID.randomUUID() + "@nutriplus.test";
        String registerBody = """
                {"name":"Before","email":"%s","password":"secret123","cpf":"%s"}
                """.formatted(email, TestCpfFactory.nextValidCpf());

        String authJson = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = extractJsonField(authJson, "token");

        mockMvc.perform(put("/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"After Update\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("After Update"))
                .andExpect(jsonPath("$.email").value(email));
    }

    private static String extractJsonField(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            throw new IllegalStateException("Field not found: " + field);
        }
        start += marker.length();
        int end = json.indexOf('"', start);
        return json.substring(start, end);
    }
}
