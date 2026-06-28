package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.LoginRequest;
import br.com.nutriplus.dto.request.RegisterRequest;
import br.com.nutriplus.dto.response.AuthResponse;
import br.com.nutriplus.dto.response.RegisterResponse;
import br.com.nutriplus.dto.response.UserResponse;
import br.com.nutriplus.service.AuthService;
import br.com.nutriplus.service.NutritionistProService;
import br.com.nutriplus.service.PasswordResetService;
import br.com.nutriplus.support.WebMvcTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest extends WebMvcTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private NutritionistProService nutritionistProService;

    @MockBean
    private PasswordResetService passwordResetService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void registerReturnsCreated() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new RegisterResponse(1L, "Test", "test@nutriplus.com", false,
                        "Seu cadastro foi recebido. Aguarde a liberação do acesso para entrar no app."));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test","email":"test@nutriplus.com","password":"secret123","cpf":"529.982.247-25","birthDate":"1990-06-15","contactPhone":"11999999999"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.loginEnabled").value(false))
                .andExpect(jsonPath("$.email").value("test@nutriplus.com"));
    }

    @Test
    void loginReturnsOk() throws Exception {
        var user = new UserResponse(1L, "Test", "test@nutriplus.com", LocalDateTime.now(), false, null, null, null, null, null);
        when(authService.login(any(LoginRequest.class)))
                .thenReturn(new AuthResponse("access", "refresh", "Bearer", 3600L, user));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginRequest("test@nutriplus.com", "secret123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access"));
    }

    @Test
    void forgotPasswordReturnsOk() throws Exception {
        when(passwordResetService.requestReset("user@nutriplus.com"))
                .thenReturn(new br.com.nutriplus.dto.response.ForgotPasswordResponse(
                        "Se o e-mail estiver cadastrado, você receberá instruções para redefinir sua senha em instantes."));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@nutriplus.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }
}
