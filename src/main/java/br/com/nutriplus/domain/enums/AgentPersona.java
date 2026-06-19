package br.com.nutriplus.domain.enums;

public enum AgentPersona {
    LUNA,
    BRUNO;

    public String toAgentId() {
        return name().toLowerCase();
    }
}
