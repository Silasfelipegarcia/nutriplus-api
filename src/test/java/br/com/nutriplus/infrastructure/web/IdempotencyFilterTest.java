package br.com.nutriplus.infrastructure.web;

import br.com.nutriplus.application.port.IdempotencyStore;
import br.com.nutriplus.domain.enums.IdempotencyStatus;
import br.com.nutriplus.infrastructure.config.IdempotencyProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdempotencyFilterTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final IdempotencyStore store = mock(IdempotencyStore.class);

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void passesThroughWhenDisabled() throws Exception {
        IdempotencyFilter filter = new IdempotencyFilter(
                new IdempotencyProperties(false, true, 24, 120),
                store,
                objectMapper
        );
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/checkins");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        verify(store, never()).find(anyString(), anyLong(), anyString(), anyString());
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void passesThroughForgotPasswordWithoutKey() throws Exception {
        IdempotencyFilter filter = new IdempotencyFilter(
                new IdempotencyProperties(true, true, 24, 120),
                store,
                objectMapper
        );
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/auth/forgot-password");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        verify(store, never()).find(anyString(), anyLong(), anyString(), anyString());
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void requiresKeyWhenEnabled() throws Exception {
        IdempotencyFilter filter = new IdempotencyFilter(
                new IdempotencyProperties(true, true, 24, 120),
                store,
                objectMapper
        );
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/checkins");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(response.getContentAsString()).contains("IDEMPOTENCY_KEY_REQUIRED");
    }

    @Test
    void replaysCompletedResponse() throws Exception {
        IdempotencyFilter filter = new IdempotencyFilter(
                new IdempotencyProperties(true, true, 24, 120),
                store,
                objectMapper
        );
        String body = "{\"ok\":true}";
        String hash = IdempotencySupport.hashRequestBody(body.getBytes());
        when(store.find(eq("key-1"), anyLong(), eq("POST"), eq("/checkins")))
                .thenReturn(Optional.of(new IdempotencyStore.StoredRecord(
                        IdempotencyStatus.COMPLETED,
                        hash,
                        201,
                        body,
                        "application/json",
                        LocalDateTime.now()
                )));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/checkins");
        request.addHeader(IdempotencySupport.HEADER, "key-1");
        request.setContent(body.getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(201);
        assertThat(response.getHeader(IdempotencySupport.REPLAYED_HEADER)).isEqualTo("true");
        assertThat(response.getContentAsString()).isEqualTo(body);
    }

    @Test
    void rejectsBodyMismatch() throws Exception {
        IdempotencyFilter filter = new IdempotencyFilter(
                new IdempotencyProperties(true, true, 24, 120),
                store,
                objectMapper
        );
        when(store.find(eq("key-1"), anyLong(), eq("POST"), eq("/checkins")))
                .thenReturn(Optional.of(new IdempotencyStore.StoredRecord(
                        IdempotencyStatus.COMPLETED,
                        "other-hash",
                        201,
                        "{}",
                        "application/json",
                        LocalDateTime.now()
                )));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/checkins");
        request.addHeader(IdempotencySupport.HEADER, "key-1");
        request.setContent("{\"status\":\"DONE\"}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(422);
        assertThat(response.getContentAsString()).contains("IDEMPOTENCY_KEY_BODY_MISMATCH");
    }

    @Test
    void storesCompletedResponseAfterSuccess() throws Exception {
        IdempotencyFilter filter = new IdempotencyFilter(
                new IdempotencyProperties(true, true, 24, 120),
                store,
                objectMapper
        );
        when(store.find(anyString(), anyLong(), anyString(), anyString())).thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/checkins");
        request.addHeader(IdempotencySupport.HEADER, "key-2");
        request.setContent("{\"mealId\":1}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (req, res) -> {
            var httpRes = (jakarta.servlet.http.HttpServletResponse) res;
            httpRes.setStatus(201);
            httpRes.getWriter().write("{\"saved\":true}");
        });

        verify(store).saveInProgress(eq("key-2"), anyLong(), eq("POST"), eq("/checkins"), anyString(), any());
        verify(store).markCompleted(
                eq("key-2"),
                anyLong(),
                eq("POST"),
                eq("/checkins"),
                eq(201),
                eq("{\"saved\":true}"),
                isNull()
        );
    }

    @Test
    void forwardsRequestBodyToDownstreamFilter() throws Exception {
        IdempotencyFilter filter = new IdempotencyFilter(
                new IdempotencyProperties(true, true, 24, 120),
                store,
                objectMapper
        );
        when(store.find(anyString(), anyLong(), anyString(), anyString())).thenReturn(Optional.empty());

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/checkins");
        request.addHeader(IdempotencySupport.HEADER, "key-body");
        request.setContent("{\"mealId\":1}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();

        final String[] capturedBody = new String[1];
        filter.doFilter(request, response, (req, res) -> {
            capturedBody[0] = req.getReader().readLine();
        });

        assertThat(capturedBody[0]).isEqualTo("{\"mealId\":1}");
    }
}
