package br.com.nutriplus.dto.request;

public class SaveCardRequest {
    private String token;
    /** CPF do titular (usado em pagamentos no Brasil quando o usuário ainda não tem CPF salvo). */
    private String cpf;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }
}
