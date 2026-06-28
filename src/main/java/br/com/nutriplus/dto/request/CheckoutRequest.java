package br.com.nutriplus.dto.request;

import br.com.nutriplus.domain.enums.SubscriptionPlan;

public class CheckoutRequest {
    private SubscriptionPlan plan;

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }
}
