package br.com.nutriplus.support;

import br.com.nutriplus.infrastructure.security.PasswordMustChangeFilter;
import br.com.nutriplus.infrastructure.security.RateLimitFilter;
import br.com.nutriplus.infrastructure.web.CorrelationIdFilter;
import br.com.nutriplus.infrastructure.web.IdempotencyFilter;
import br.com.nutriplus.infrastructure.web.MdcUserFilter;
import br.com.nutriplus.infrastructure.web.RequestPerformanceFilter;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Mocks de filtros customizados para {@code @WebMvcTest} no pacote {@code br.com.nutriplus.*},
 * onde o contexto herda {@code NutriplusApplication} e tenta instanciar beans de infraestrutura.
 */
public abstract class WebMvcTestSupport {

    @MockBean
    private RateLimitFilter rateLimitFilter;

    @MockBean
    private CorrelationIdFilter correlationIdFilter;

    @MockBean
    private RequestPerformanceFilter requestPerformanceFilter;

    @MockBean
    private MdcUserFilter mdcUserFilter;

    @MockBean
    private PasswordMustChangeFilter passwordMustChangeFilter;

    @MockBean
    private IdempotencyFilter idempotencyFilter;
}
