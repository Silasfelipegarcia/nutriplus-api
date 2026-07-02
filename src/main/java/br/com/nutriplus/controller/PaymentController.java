package br.com.nutriplus.controller;

import br.com.nutriplus.domain.enums.SubscriptionPlan;
import br.com.nutriplus.dto.request.*;
import br.com.nutriplus.dto.response.*;
import br.com.nutriplus.payment.MercadoPagoPaymentService;
import br.com.nutriplus.payment.MercadoPagoWebhookVerifier;
import br.com.nutriplus.security.AuthorizationService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final MercadoPagoPaymentService paymentService;
    private final MercadoPagoWebhookVerifier webhookVerifier;
    private final AuthorizationService authorizationService;

    public PaymentController(MercadoPagoPaymentService paymentService,
                             MercadoPagoWebhookVerifier webhookVerifier,
                             AuthorizationService authorizationService) {
        this.paymentService = paymentService;
        this.webhookVerifier = webhookVerifier;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/checkout")
    public ResponseEntity<CheckoutResponse> checkout(@RequestBody CheckoutRequest request) {
        Long userId = authorizationService.currentUserId();
        return ResponseEntity.ok(paymentService.criarCheckout(userId, request.getPlan()));
    }

    @PostMapping("/checkout/sync")
    public ResponseEntity<CheckoutSyncResponse> sincronizarCheckout(@RequestBody CheckoutSyncRequest request) {
        Long userId = authorizationService.currentUserId();
        return ResponseEntity.ok(paymentService.sincronizarCheckoutRetorno(userId, request));
    }

    @GetMapping("/quote")
    public ResponseEntity<PlanQuoteResponse> cotacao(@RequestParam SubscriptionPlan plan) {
        return ResponseEntity.ok(paymentService.obterCotacao(authorizationService.currentUserId(), plan));
    }

    @GetMapping("/config")
    public ResponseEntity<PaymentConfigResponse> config() {
        return ResponseEntity.ok(paymentService.obterConfig());
    }

    @GetMapping("/history")
    public ResponseEntity<List<PaymentHistoryItemResponse>> historico() {
        return ResponseEntity.ok(paymentService.listarHistorico(authorizationService.currentUserId()));
    }

    @GetMapping("/cards")
    public ResponseEntity<List<SavedCardResponse>> cartoes() {
        return ResponseEntity.ok(paymentService.listarCartoes(authorizationService.currentUserId()));
    }

    @PostMapping("/cards")
    public ResponseEntity<SavedCardResponse> salvarCartao(@RequestBody SaveCardRequest request) {
        Long userId = authorizationService.currentUserId();
        SavedCardResponse card = paymentService.salvarCartao(userId, request.getToken(), request.getCpf());
        return ResponseEntity.status(201).body(card);
    }

    @DeleteMapping("/cards/{cardId}")
    public ResponseEntity<Void> removerCartao(@PathVariable String cardId) {
        paymentService.removerCartao(authorizationService.currentUserId(), cardId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/charge")
    public ResponseEntity<ChargePlanResponse> cobrar(@RequestBody ChargePlanRequest request) {
        Long userId = authorizationService.currentUserId();
        return ResponseEntity.ok(paymentService.cobrarPlano(userId, request));
    }

    @GetMapping("/subscription")
    public ResponseEntity<SubscriptionStatusResponse> assinatura() {
        return ResponseEntity.ok(paymentService.obterStatusAssinatura(authorizationService.currentUserId()));
    }

    @PostMapping("/subscription/cancel")
    public ResponseEntity<SubscriptionStatusResponse> cancelarAssinatura() {
        Long userId = authorizationService.currentUserId();
        paymentService.cancelarAssinatura(userId);
        return ResponseEntity.ok(paymentService.obterStatusAssinatura(userId));
    }

    @PostMapping("/subscription/reactivate")
    public ResponseEntity<SubscriptionStatusResponse> reativarAssinatura() {
        Long userId = authorizationService.currentUserId();
        paymentService.reativarAssinatura(userId);
        return ResponseEntity.ok(paymentService.obterStatusAssinatura(userId));
    }

    @PostMapping("/mercadopago/webhook")
    public ResponseEntity<Void> webhookPost(@RequestParam(value = "topic", required = false) String topic,
                                            @RequestParam(value = "id", required = false) String id,
                                            @RequestParam(value = "type", required = false) String type,
                                            @RequestParam(value = "data.id", required = false) String dataId,
                                            @RequestBody(required = false) JsonNode body,
                                            HttpServletRequest request) {
        String paymentId = resolverIdWebhook(id, dataId, body);
        String evento = topic != null ? topic : (type != null ? type : extrairTipoWebhook(body));
        if (!webhookVerifier.verificar(request, paymentId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        processar(evento, paymentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/mercadopago/webhook")
    public ResponseEntity<Void> webhookGet(@RequestParam(value = "topic", required = false) String topic,
                                           @RequestParam(value = "id", required = false) String id,
                                           HttpServletRequest request) {
        if (!webhookVerifier.verificar(request, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        processar(topic, id);
        return ResponseEntity.ok().build();
    }

    private void processar(String topic, String id) {
        if (id == null || id.isBlank()) {
            return;
        }
        if (topic == null || topic.isBlank()
                || "payment".equalsIgnoreCase(topic)
                || "merchant_order".equalsIgnoreCase(topic)) {
            paymentService.processarNotificacao(id);
        }
    }

    private String resolverIdWebhook(String id, String dataId, JsonNode body) {
        if (id != null && !id.isBlank()) {
            return id;
        }
        if (dataId != null && !dataId.isBlank()) {
            return dataId;
        }
        if (body != null && body.has("data") && body.get("data").has("id")) {
            return body.get("data").get("id").asText(null);
        }
        return null;
    }

    private String extrairTipoWebhook(JsonNode body) {
        if (body != null && body.has("type")) {
            return body.get("type").asText(null);
        }
        if (body != null && body.has("action")) {
            return body.get("action").asText(null);
        }
        return null;
    }
}
