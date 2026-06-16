package br.com.nutriplus.exception;

public class AiAgentException extends RuntimeException {
    public AiAgentException(String message) {
        super(message);
    }

    public AiAgentException(String message, Throwable cause) {
        super(message, cause);
    }
}
