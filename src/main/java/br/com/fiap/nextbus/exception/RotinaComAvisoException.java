package br.com.fiap.nextbus.exception;

public class RotinaComAvisoException extends RuntimeException {

    public RotinaComAvisoException() {
        super("Esta rotina já tem aviso e não pode ser excluída.");
    }
}
