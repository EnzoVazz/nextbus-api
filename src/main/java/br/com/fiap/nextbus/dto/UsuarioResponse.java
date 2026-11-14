package br.com.fiap.nextbus.dto;

import br.com.fiap.nextbus.model.Papel;

public class UsuarioResponse {

    private Long id;
    private String nome;
    private String email;
    private Papel papel;

    public UsuarioResponse() {
    }

    public UsuarioResponse(Long id, String nome, String email, Papel papel) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.papel = papel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Papel getPapel() { return papel; }
    public void setPapel(Papel papel) { this.papel = papel; }
}
