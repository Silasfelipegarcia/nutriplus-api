package br.com.nutriplus.payment;

import br.com.nutriplus.domain.entity.User;
import br.com.nutriplus.infrastructure.config.MercadoPagoProperties;
import br.com.nutriplus.infrastructure.security.CpfProtectionService;
import br.com.nutriplus.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class MercadoPagoCustomerService {

    private static final Logger log = LoggerFactory.getLogger(MercadoPagoCustomerService.class);

    private final MercadoPagoProperties properties;
    private final UserRepository userRepository;
    private final CpfProtectionService cpfProtectionService;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public MercadoPagoCustomerService(MercadoPagoProperties properties,
                                      UserRepository userRepository,
                                      CpfProtectionService cpfProtectionService,
                                      ObjectMapper objectMapper) {
        this.properties = properties;
        this.userRepository = userRepository;
        this.cpfProtectionService = cpfProtectionService;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(properties.apiBaseUrl())
                .build();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public User obterOuCriar(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (user.getMpCustomerId() != null && !user.getMpCustomerId().isBlank()) {
            return user;
        }

        Map<String, Object> body = Map.of("email", user.getEmail());
        try {
            JsonNode response = restClient.post()
                    .uri("/v1/customers")
                    .header("Authorization", "Bearer " + properties.accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("id")) {
                throw new IllegalStateException("Falha ao criar cliente no Mercado Pago");
            }

            user.setMpCustomerId(response.get("id").asText());
            User saved = userRepository.save(user);
            sincronizarPerfil(saved);
            return saved;
        } catch (RestClientResponseException e) {
            if (clienteJaExiste(e)) {
                String customerId = buscarIdPorEmail(user.getEmail());
                if (customerId != null) {
                    user.setMpCustomerId(customerId);
                    User saved = userRepository.save(user);
                    sincronizarPerfil(saved);
                    return saved;
                }
            }
            throw traduzirErro("Falha ao vincular cliente no Mercado Pago", e);
        }
    }

    public String resolverCpf(User user) {
        if (user.getCpfEncrypted() == null || user.getCpfEncrypted().isBlank()) {
            return null;
        }
        try {
            return cpfProtectionService.decrypt(user.getCpfEncrypted()).replaceAll("\\D", "");
        } catch (Exception e) {
            log.warn("Não foi possível descriptografar CPF do usuário {}", user.getId());
            return null;
        }
    }

    public void sincronizarPerfil(User user) {
        if (!properties.isConfigured() || properties.isMockMode()) {
            return;
        }
        if (user.getMpCustomerId() == null || user.getMpCustomerId().isBlank()) {
            return;
        }

        Map<String, Object> body = new HashMap<>();
        body.put("email", user.getEmail());
        adicionarNome(body, user.getName());
        String cpf = resolverCpf(user);
        if (cpf != null && !cpf.isBlank()) {
            body.put("identification", Map.of("type", "CPF", "number", cpf));
        }
        Map<String, Object> phone = montarTelefone(user.getContactPhone());
        if (phone != null) {
            body.put("phone", phone);
        }

        try {
            restClient.put()
                    .uri("/v1/customers/{id}", user.getMpCustomerId())
                    .header("Authorization", "Bearer " + properties.accessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException e) {
            log.warn("Falha ao sincronizar cliente MP {}: {}", user.getId(), e.getMessage());
        }
    }

    private static void adicionarNome(Map<String, Object> body, String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return;
        }
        String trimmed = fullName.trim();
        int space = trimmed.indexOf(' ');
        if (space > 0) {
            body.put("first_name", trimmed.substring(0, space));
            body.put("last_name", trimmed.substring(space + 1).trim());
        } else {
            body.put("first_name", trimmed);
            body.put("last_name", trimmed);
        }
    }

    private static Map<String, Object> montarTelefone(String contactPhone) {
        if (contactPhone == null || contactPhone.isBlank()) {
            return null;
        }
        String digits = contactPhone.replaceAll("\\D", "");
        if (digits.startsWith("55") && digits.length() >= 12) {
            digits = digits.substring(2);
        }
        if (digits.length() < 10) {
            return null;
        }
        return Map.of(
                "area_code", digits.substring(0, 2),
                "number", digits.substring(2));
    }

    private boolean clienteJaExiste(RestClientResponseException e) {
        return e.getStatusCode().value() == 400
                && e.getResponseBodyAsString().contains("the customer already exist");
    }

    private String buscarIdPorEmail(String email) {
        try {
            JsonNode response = restClient.get()
                    .uri("/v1/customers/search?email={email}", email)
                    .header("Authorization", "Bearer " + properties.accessToken())
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("results") || !response.get("results").isArray()) {
                return null;
            }
            JsonNode results = response.get("results");
            if (results.isEmpty()) {
                return null;
            }
            return results.get(0).path("id").asText(null);
        } catch (RestClientResponseException e) {
            log.warn("Falha ao buscar cliente MP por e-mail {}: {}", email, e.getMessage());
            return null;
        }
    }

    IllegalArgumentException traduzirErro(String prefixo, RestClientResponseException e) {
        log.warn("Mercado Pago {}: {} {}", prefixo, e.getStatusCode(), e.getResponseBodyAsString());
        String detalhe = extrairMensagem(e.getResponseBodyAsString());
        String body = e.getResponseBodyAsString() != null ? e.getResponseBodyAsString() : "";
        if (body.contains("\"code\":300")
                || detalhe.toLowerCase(Locale.ROOT).contains("live credentials")
                || detalhe.toLowerCase(Locale.ROOT).contains("access denied")) {
            return new IllegalArgumentException(
                    "O Mercado Pago não permite salvar cartões com credenciais de teste (API de clientes bloqueada). "
                            + "Reinicie a API com a versão mais recente para usar o cofre local em desenvolvimento, "
                            + "ou use Assinar com Mercado Pago (Checkout Pro).");
        }
        if (detalhe.toLowerCase(Locale.ROOT).contains("card not found")) {
            return new IllegalArgumentException(
                    "Cartão salvo inválido ou expirado no Mercado Pago. Remova e cadastre novamente em Cobrança.");
        }
        if (detalhe.isBlank()) {
            return new IllegalArgumentException(prefixo + ". Tente novamente.");
        }
        return new IllegalArgumentException(prefixo + ": " + detalhe);
    }

    private String extrairMensagem(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        try {
            JsonNode node = objectMapper.readTree(body);
            if (node.has("message")) {
                return node.get("message").asText().trim();
            }
            if (node.has("cause") && node.get("cause").isArray() && !node.get("cause").isEmpty()) {
                return node.get("cause").get(0).path("description").asText("");
            }
        } catch (Exception ignored) {
            // fallback
        }
        return "";
    }
}
