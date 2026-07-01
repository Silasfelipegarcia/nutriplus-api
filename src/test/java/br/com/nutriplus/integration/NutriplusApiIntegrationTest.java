package br.com.nutriplus.integration;

import br.com.nutriplus.AbstractIntegrationTest;
import br.com.nutriplus.support.IntegrationAuthSupport;
import br.com.nutriplus.support.TestCpfFactory;
import br.com.nutriplus.support.TestRegisterFactory;
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

        String token = IntegrationAuthSupport.registerAndLogin(
                mockMvc,
                userRepository,
                "Integration User",
                email,
                password,
                TestCpfFactory.nextValidCpf());

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
        String token = IntegrationAuthSupport.registerAndLogin(
                mockMvc,
                userRepository,
                "Before",
                email,
                "secret123",
                TestCpfFactory.nextValidCpf());

        mockMvc.perform(put("/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"After Update\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("After Update"))
                .andExpect(jsonPath("$.email").value(email));
    }

    @Test
    void registerRejectsDuplicateEmail() throws Exception {
        String email = "dup-email-" + UUID.randomUUID() + "@nutriplus.test";
        String cpf = TestCpfFactory.nextValidCpf();

        IntegrationAuthSupport.registerOnly(mockMvc, "First User", email, "secret123", cpf);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestRegisterFactory.body(
                                "Second User", email, "secret123", TestCpfFactory.nextValidCpf())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("E-mail já cadastrado"));
    }

    @Test
    void registerRejectsDuplicateCpf() throws Exception {
        String cpf = TestCpfFactory.nextValidCpf();
        IntegrationAuthSupport.registerOnly(
                mockMvc,
                "First User",
                "first-%s@nutriplus.test".formatted(UUID.randomUUID()),
                "secret123",
                cpf);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestRegisterFactory.body(
                                "Second User",
                                "second-%s@nutriplus.test".formatted(UUID.randomUUID()),
                                "secret123",
                                cpf)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("CPF já cadastrado"));
    }
}
