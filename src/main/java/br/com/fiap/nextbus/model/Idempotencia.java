package br.com.fiap.nextbus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "IDEMPOTENCIA")
public class Idempotencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_idempotencia")
    private Long id;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "chave", nullable = false, length = 80)
    private String chave;

    @Column(name = "hash_corpo", nullable = false, length = 128)
    private String hashCorpo;

    @Column(name = "id_rotina", nullable = false)
    private Long idRotina;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Long idUsuario) { this.idUsuario = idUsuario; }

    public String getChave() { return chave; }
    public void setChave(String chave) { this.chave = chave; }

    public String getHashCorpo() { return hashCorpo; }
    public void setHashCorpo(String hashCorpo) { this.hashCorpo = hashCorpo; }

    public Long getIdRotina() { return idRotina; }
    public void setIdRotina(Long idRotina) { this.idRotina = idRotina; }
}
