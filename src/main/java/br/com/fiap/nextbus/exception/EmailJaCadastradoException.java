package br.com.fiap.nextbus.exception;

public class EmailJaCadastradoException extends RuntimeException {

    public EmailJaCadastradoException() {
        super("Este email já está cadastrado.");
    }
}
