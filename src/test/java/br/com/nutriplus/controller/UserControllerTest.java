package br.com.nutriplus.controller;

import br.com.nutriplus.dto.request.DeleteAccountRequest;
import br.com.nutriplus.infrastructure.security.WebPortalClientVerifier;
import br.com.nutriplus.service.UserService;
import br.com.nutriplus.support.WebMvcTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest extends WebMvcTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private WebPortalClientVerifier webPortalClientVerifier;

    @Test
    void deleteAccountReturnsNoContent() throws Exception {
        doNothing().when(webPortalClientVerifier).requireWebPortal(any());
        doNothing().when(userService).deleteAccount(any());

        mockMvc.perform(delete("/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(WebPortalClientVerifier.CLIENT_HEADER, WebPortalClientVerifier.CLIENT_WEB)
                        .header("Origin", "http://localhost:4200")
                        .content(objectMapper.writeValueAsString(
                                new DeleteAccountRequest("senha123", "user@example.com"))))
                .andExpect(status().isNoContent());

        verify(webPortalClientVerifier).requireWebPortal(any());
        verify(userService).deleteAccount(any(DeleteAccountRequest.class));
    }
}
