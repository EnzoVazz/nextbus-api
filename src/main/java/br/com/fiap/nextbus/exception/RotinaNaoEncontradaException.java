package br.com.fiap.nextbus.exception;

public class RotinaNaoEncontradaException extends RuntimeException {

    public RotinaNaoEncontradaException() {
        super("Rotina não encontrada.");
    }
}
