package br.com.nutriplus.integration;

import br.com.nutriplus.AbstractIntegrationTest;
import br.com.nutriplus.support.TestCpfFactory;
import br.com.nutriplus.support.TestRegisterFactory;
import br.com.nutriplus.infrastructure.web.IdempotencySupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource(properties = {
        "idempotency.enabled=true",
        "idempotency.require-key=true"
})
class IdempotencyIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerRequiresIdempotencyKeyWhenEnabled() throws Exception {
        String email = "idem-" + UUID.randomUUID() + "@nutriplus.test";
        String registerBody = TestRegisterFactory.body("Idem User", email, "secret123", TestCpfFactory.nextValidCpf());

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("IDEMPOTENCY_KEY_REQUIRED"));
    }

    @Test
    void registerReplayReturnsSameResponse() throws Exception {
        String email = "idem-replay-" + UUID.randomUUID() + "@nutriplus.test";
        String cpf = TestCpfFactory.nextValidCpf();
        String registerBody = TestRegisterFactory.body("Replay User", email, "secret123", cpf);
        String key = UUID.randomUUID().toString();

        mockMvc.perform(post("/auth/register")
                        .header(IdempotencySupport.HEADER, key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andExpect(jsonPath("$.user.email").value(email));

        mockMvc.perform(post("/auth/register")
                        .header(IdempotencySupport.HEADER, key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerBody))
                .andExpect(status().isCreated())
                .andExpect(header().string(IdempotencySupport.REPLAYED_HEADER, "true"))
                .andExpect(jsonPath("$.user.email").value(email));
    }
}
