package br.com.nutriplus.dto.response;

import br.com.nutriplus.domain.enums.SubscriptionPlan;

import java.util.List;

public class PlanCatalogItemResponse {
    private SubscriptionPlan plan;
    private String nome;
    private String descricao;
    private int priceCents;
    private String priceLabel;
    private List<String> beneficios;
    private boolean contatoComercial;
    private boolean trialDisponivel;

    public SubscriptionPlan getPlan() { return plan; }
    public void setPlan(SubscriptionPlan plan) { this.plan = plan; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public int getPriceCents() { return priceCents; }
    public void setPriceCents(int priceCents) { this.priceCents = priceCents; }
    public String getPriceLabel() { return priceLabel; }
    public void setPriceLabel(String priceLabel) { this.priceLabel = priceLabel; }
    public List<String> getBeneficios() { return beneficios; }
    public void setBeneficios(List<String> beneficios) { this.beneficios = beneficios; }
    public boolean isContatoComercial() { return contatoComercial; }
    public void setContatoComercial(boolean contatoComercial) { this.contatoComercial = contatoComercial; }
    public boolean isTrialDisponivel() { return trialDisponivel; }
    public void setTrialDisponivel(boolean trialDisponivel) { this.trialDisponivel = trialDisponivel; }
}
