package br.com.fiap.nextbus.exception;

public class RotinaDuplicadaException extends RuntimeException {

    public RotinaDuplicadaException() {
        super("Já existe uma rotina ativa com essa linha, ponto e horário.");
    }
}
