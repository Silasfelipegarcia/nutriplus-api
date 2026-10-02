package br.com.nutriplus.integration;

import br.com.nutriplus.AbstractIntegrationTest;
import br.com.nutriplus.repository.NutritionistRepository;
import br.com.nutriplus.support.IntegrationAuthSupport;
import br.com.nutriplus.support.TestCpfFactory;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProProfileIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NutritionistRepository nutritionistRepository;

    @Test
    void getProProfileSucceedsWhenUserWouldBeLazyWithoutFetch() throws Exception {
        String email = "pro-profile-" + UUID.randomUUID() + "@nutriplus.test";
        String token = IntegrationAuthSupport.registerNutritionistAndLogin(
                mockMvc, userRepository, "Ana Nutri", email, "secret123",
                TestCpfFactory.nextValidCpf(), "CRN-SP 12345");
        var auth = IntegrationAuthSupport.bearerHeaders(token);

        var user = userRepository.findByEmail(email).orElseThrow();
        var nutritionist = nutritionistRepository.findByUserId(user.getId()).orElseThrow();
        assertThat(Hibernate.isInitialized(nutritionist.getUser())).isTrue();
        assertThat(nutritionist.getUser().getName()).isEqualTo("Ana Nutri");

        mockMvc.perform(get("/pro/profile").headers(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Ana Nutri"))
                .andExpect(jsonPath("$.crn").value("CRN-SP 12345"));
    }

    @Test
    void getProProfileAndMeDoNotFailWhenCpfDecryptFails() throws Exception {
        String email = "pro-cpf-" + UUID.randomUUID() + "@nutriplus.test";
        String token = IntegrationAuthSupport.registerNutritionistAndLogin(
                mockMvc, userRepository, "Bruno Nutri", email, "secret123",
                TestCpfFactory.nextValidCpf(), "CRN-RJ 67890");
        var auth = IntegrationAuthSupport.bearerHeaders(token);

        var user = userRepository.findByEmail(email).orElseThrow();
        user.setCpfEncrypted("not-valid-ciphertext");
        userRepository.saveAndFlush(user);

        mockMvc.perform(get("/pro/profile").headers(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bruno Nutri"));

        mockMvc.perform(get("/users/me").headers(auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.cpfMasked").value(org.hamcrest.Matchers.nullValue()));
    }
}
