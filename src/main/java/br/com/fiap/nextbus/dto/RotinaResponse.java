package br.com.fiap.nextbus.dto;

import br.com.fiap.nextbus.model.Rotina;
import br.com.fiap.nextbus.model.Sentido;

public class RotinaResponse {

    private Long id;
    private String codigoLinha;
    private String letreiro;
    private Sentido sentido;
    private String codigoParada;
    private String nomeParada;
    private String diasSemana;
    private String horarioEmbarque;
    private Boolean ativa;

    public RotinaResponse() {
    }

    public RotinaResponse(Rotina rotina) {
        this.id = rotina.getId();
        this.codigoLinha = rotina.getCodigoLinha();
        this.letreiro = rotina.getLetreiro();
        this.sentido = rotina.getSentido();
        this.codigoParada = rotina.getCodigoParada();
        this.nomeParada = rotina.getNomeParada();
        this.diasSemana = rotina.getDiasSemana();
        this.horarioEmbarque = rotina.getHorarioEmbarque();
        this.ativa = rotina.getAtiva();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getHorarioEmbarque() { return horarioEmbarque; }
    public void setHorarioEmbarque(String horarioEmbarque) { this.horarioEmbarque = horarioEmbarque; }

    public Boolean getAtiva() { return ativa; }
    public void setAtiva(Boolean ativa) { this.ativa = ativa; }
}
