package br.com.fiap.nextbus.dto;

import java.util.List;

public class ErroResponse {

    private int status;
    private String erro;
    private List<String> mensagens;

    public ErroResponse() {
    }

    public ErroResponse(int status, String erro, List<String> mensagens) {
        this.status = status;
        this.erro = erro;
        this.mensagens = mensagens;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getErro() { return erro; }
    public void setErro(String erro) { this.erro = erro; }

    public List<String> getMensagens() { return mensagens; }
    public void setMensagens(List<String> mensagens) { this.mensagens = mensagens; }
}
