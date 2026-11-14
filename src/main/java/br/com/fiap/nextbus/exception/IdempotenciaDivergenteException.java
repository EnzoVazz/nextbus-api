package br.com.fiap.nextbus.exception;

public class IdempotenciaDivergenteException extends RuntimeException {

    public IdempotenciaDivergenteException() {
        super("Essa Idempotency-Key já foi usada com outros dados.");
    }
}
