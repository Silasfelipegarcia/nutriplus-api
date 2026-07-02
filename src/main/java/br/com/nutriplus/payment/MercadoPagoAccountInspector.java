package br.com.nutriplus.payment;

import br.com.nutriplus.infrastructure.config.MercadoPagoProperties;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Detecta limitações da conta Mercado Pago (ex.: vendedor de teste não pode usar POST /v1/customers).
 */
@Component
public class MercadoPagoAccountInspector {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoAccountInspector.class);

    private final MercadoPagoProperties properties;
    private final RestClient restClient;
    private volatile Boolean testSellerAccount;

    public MercadoPagoAccountInspector(MercadoPagoProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl(properties.apiBaseUrl())
                .build();
    }

    @PostConstruct
    void probeOnStartup() {
        if (!properties.isCheckoutReady()) {
            log.warn(
                    "Mercado Pago: pagamentos indisponíveis — defina MERCADOPAGO_ACCESS_TOKEN e MERCADOPAGO_PUBLIC_KEY "
                            + "(e MERCADOPAGO_MOCK_MODE=false em produção).");
        } else if (properties.mockMode()) {
            log.warn(
                    "Mercado Pago: MERCADOPAGO_MOCK_MODE=true — checkout real desativado; "
                            + "use false em produção ou confira cofre local para credenciais de teste.");
        }
        refresh();
    }

    public void refresh() {
        if (!properties.isConfigured() || properties.isMockMode()) {
            testSellerAccount = null;
            return;
        }
        try {
            JsonNode response = restClient.get()
                    .uri("/users/me")
                    .header("Authorization", "Bearer " + properties.accessToken())
                    .retrieve()
                    .body(JsonNode.class);
            boolean testUser = response != null
                    && response.path("test_data").path("test_user").asBoolean(false);
            testSellerAccount = testUser;
            if (testUser) {
                log.warn(
                        "Mercado Pago: credenciais de TESTE detectadas (vendedor test_user). "
                                + "A API de clientes/cartões do MP não aceita POST /v1/customers neste modo — "
                                + "cofre de cartões local ativado para desenvolvimento.");
            }
        } catch (RestClientResponseException e) {
            log.warn("Não foi possível inspecionar conta Mercado Pago: {}", e.getMessage());
            testSellerAccount = null;
        }
    }

    public boolean isTestSellerAccount() {
        return Boolean.TRUE.equals(testSellerAccount);
    }

    /** Cofre local (sem MP customers/cards) — mock total ou conta de teste do MP. */
    public boolean useCardVaultMock() {
        if (properties.isMockMode() || properties.cardVaultMock()) {
            return true;
        }
        return isTestSellerAccount();
    }
}
