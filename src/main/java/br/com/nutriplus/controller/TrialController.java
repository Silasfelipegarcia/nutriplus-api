package br.com.nutriplus.controller;

import br.com.nutriplus.dto.response.SubscriptionStatusResponse;
import br.com.nutriplus.payment.MercadoPagoPaymentService;
import br.com.nutriplus.security.AuthorizationService;
import br.com.nutriplus.service.TrialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payments")
public class TrialController {

    private final TrialService trialService;
    private final MercadoPagoPaymentService paymentService;
    private final AuthorizationService authorizationService;

    public TrialController(TrialService trialService,
                           MercadoPagoPaymentService paymentService,
                           AuthorizationService authorizationService) {
        this.trialService = trialService;
        this.paymentService = paymentService;
        this.authorizationService = authorizationService;
    }

    @PostMapping("/trial")
    public ResponseEntity<SubscriptionStatusResponse> iniciarTrial() {
        Long userId = authorizationService.currentUserId();
        trialService.iniciarTrial(userId);
        return ResponseEntity.ok(paymentService.obterStatusAssinatura(userId));
    }
}
