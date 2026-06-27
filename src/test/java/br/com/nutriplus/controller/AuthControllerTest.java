package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.LoginRequest;
import br.com.nutriplus.dto.request.RegisterRequest;
import br.com.nutriplus.dto.response.AuthResponse;
import br.com.nutriplus.dto.response.UserResponse;
import br.com.nutriplus.service.AuthService;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void registerReturnsCreated() throws Exception {
        var user = new UserResponse(1L, "Test", "test@nutriplus.com", LocalDateTime.now(), false, null, null, null, null, null);
        when(authService.register(any(RegisterRequest.class)))
                .thenReturn(new AuthResponse("access", "refresh", "Bearer", 3600L, user));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Test","email":"test@nutriplus.com","password":"secret123","cpf":"529.982.247-25","birthDate":"1990-06-15"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("access"))
                .andExpect(jsonPath("$.user.email").value("test@nutriplus.com"));
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
}
