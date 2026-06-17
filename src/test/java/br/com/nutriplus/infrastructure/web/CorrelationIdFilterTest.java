package br.com.nutriplus.infrastructure.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void generatesCorrelationAndTraceWhenMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isNotBlank();
        assertThat(response.getHeader(CorrelationIdFilter.TRACE_HEADER)).isNotBlank();
    }

    @Test
    void propagatesIncomingHeaders() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/health");
        request.addHeader(CorrelationIdFilter.HEADER, "cid-123");
        request.addHeader(CorrelationIdFilter.TRACE_HEADER, "trace-456");
        request.addHeader(CorrelationIdFilter.FLOW_HEADER, "login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getHeader(CorrelationIdFilter.HEADER)).isEqualTo("cid-123");
        assertThat(response.getHeader(CorrelationIdFilter.TRACE_HEADER)).isEqualTo("trace-456");
    }
}
