package br.com.nutriplus.infrastructure.observability;

import br.com.nutriplus.infrastructure.web.CorrelationIdFilter;
import br.com.nutriplus.infrastructure.web.MdcUserFilter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import static org.assertj.core.api.Assertions.assertThatCode;

class NewRelicTraceBridgeTest {

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void syncFromMdcDoesNotThrowWithoutAgent() {
        MDC.put(CorrelationIdFilter.MDC_KEY, "cid-test");
        MDC.put(CorrelationIdFilter.MDC_TRACE, "trace-test");
        MDC.put(MdcUserFilter.MDC_USER, "user-1");

        assertThatCode(NewRelicTraceBridge::syncFromMdc).doesNotThrowAnyException();
    }

    @Test
    void syncHttpContextDoesNotThrowWithoutAgent() {
        assertThatCode(() -> NewRelicTraceBridge.syncHttpContext("GET", "/users/me", 200, 42L))
                .doesNotThrowAnyException();
    }

    @Test
    void noticeErrorDoesNotThrowWithoutAgent() {
        assertThatCode(() -> NewRelicTraceBridge.noticeError(new RuntimeException("test")))
                .doesNotThrowAnyException();
    }
}
