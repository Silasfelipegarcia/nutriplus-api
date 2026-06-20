package br.com.nutriplus.controller;

import br.com.nutriplus.service.StripePaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhooks")
public class StripeWebhookController {

    private final StripePaymentService stripePaymentService;

    public StripeWebhookController(StripePaymentService stripePaymentService) {
        this.stripePaymentService = stripePaymentService;
    }

    @PostMapping("/stripe")
    public void handle(@RequestBody String payload,
                       @RequestHeader(value = "Stripe-Signature", required = false) String sig) {
        stripePaymentService.handleWebhook(payload, sig);
    }
}
