package br.com.fiap.nextbus.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "ROTINA")
public class Rotina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rotina")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "codigo_linha", nullable = false, length = 20)
    private String codigoLinha;

    @Column(name = "letreiro", nullable = false, length = 20)
    private String letreiro;

    @Convert(converter = SentidoConverter.class)
    @Column(name = "sentido", nullable = false)
    private Sentido sentido;

    @Column(name = "codigo_parada", nullable = false, length = 20)
    private String codigoParada;

    @Column(name = "nome_parada", nullable = false, length = 160)
    private String nomeParada;

    @Column(name = "dias_semana", nullable = false, length = 20)
    private String diasSemana;

    @Column(name = "horario_embarque", nullable = false, length = 5)
    private String horarioEmbarque;

    @Convert(converter = SimNaoConverter.class)
    @Column(name = "ativa", nullable = false, length = 1)
    private Boolean ativa = Boolean.TRUE;

    public Rotina() {
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getCodigoLinha() { return codigoLinha; }
    public void setCodigoLinha(String codigoLinha) { this.codigoLinha = codigoLinha; }

    public String getLetreiro() { return letreiro; }
    public void setLetreiro(String letreiro) { this.letreiro = letreiro; }

    public Sentido getSentido() { return sentido; }
    public void setSentido(Sentido sentido) { this.sentido = sentido; }

    public String getCodigoParada() { return codigoParada; }
    public void setCodigoParada(String codigoParada) { this.codigoParada = codigoParada; }

    public String getNomeParada() { return nomeParada; }
    public void setNomeParada(String nomeParada) { this.nomeParada = nomeParada; }

    public String getDiasSemana() { return diasSemana; }
    public void setDiasSemana(String diasSemana) { this.diasSemana = diasSemana; }

    public String getHorarioEmbarque() {
        return horarioEmbarque == null ? null : horarioEmbarque.trim();
    }

    public void setHorarioEmbarque(String horarioEmbarque) {
        this.horarioEmbarque = horarioEmbarque == null ? null : horarioEmbarque.trim();
    }

    public Boolean getAtiva() { return ativa; }
    public void setAtiva(Boolean ativa) { this.ativa = ativa; }
}
