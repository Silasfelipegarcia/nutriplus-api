package br.com.nutriplus.exception;

public class LoginDisabledException extends RuntimeException {

    public LoginDisabledException(String message) {
        super(message);
    }
}
