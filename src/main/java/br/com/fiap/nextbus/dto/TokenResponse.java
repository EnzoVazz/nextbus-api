package br.com.fiap.nextbus.dto;

import br.com.fiap.nextbus.model.Papel;

public class TokenResponse {

    private String token;
    private String tipo = "Bearer";
    private Long id;
    private String nome;
    private String email;
    private Papel papel;

    public TokenResponse() {
    }

    public TokenResponse(String token, Long id, String nome, String email, Papel papel) {
        this.token = token;
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.papel = papel;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Papel getPapel() { return papel; }
    public void setPapel(Papel papel) { this.papel = papel; }
}
