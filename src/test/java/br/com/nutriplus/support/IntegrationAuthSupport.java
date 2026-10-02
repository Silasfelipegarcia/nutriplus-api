package br.com.nutriplus.support;

import br.com.nutriplus.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public final class IntegrationAuthSupport {

    private IntegrationAuthSupport() {
    }

    public static String registerNutritionistAndLogin(
            MockMvc mockMvc,
            UserRepository userRepository,
            String name,
            String email,
            String password,
            String cpf,
            String crn
    ) throws Exception {
        String body = """
                {"name":"%s","email":"%s","password":"%s","cpf":"%s","contactPhone":"11999999999","crn":"%s","bio":"Bio de teste","specialties":"Emagrecimento"}
                """.formatted(name, email, password, cpf, crn);

        mockMvc.perform(post("/auth/register/nutritionist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.loginEnabled").value(false));

        var user = userRepository.findByEmail(email).orElseThrow();
        user.setLoginEnabled(true);
        user.setLoginEnabledAt(LocalDateTime.now());
        userRepository.save(user);

        return loginAs(mockMvc, email, password);
    }

    public static String registerAndLogin(
            MockMvc mockMvc,
            UserRepository userRepository,
            String name,
            String email,
            String password,
            String cpf
    ) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestRegisterFactory.body(name, email, password, cpf)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.loginEnabled").value(false));

        var user = userRepository.findByEmail(email).orElseThrow();
        user.setLoginEnabled(true);
        user.setLoginEnabledAt(LocalDateTime.now());
        userRepository.save(user);

        String loginBody = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        String authJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonTestSupport.extractStringField(authJson, "token");
    }

    public static String loginAs(MockMvc mockMvc, String email, String password) throws Exception {
        String loginBody = """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);

        String authJson = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(notNullValue()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return JsonTestSupport.extractStringField(authJson, "token");
    }

    public static org.springframework.http.HttpHeaders bearerHeaders(String token) {
        var headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        return headers;
    }

    public static void registerOnly(MockMvc mockMvc, String name, String email, String password, String cpf)
            throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(TestRegisterFactory.body(name, email, password, cpf)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.loginEnabled").value(false));
    }
}
