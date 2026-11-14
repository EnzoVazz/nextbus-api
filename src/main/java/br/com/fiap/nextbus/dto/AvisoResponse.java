package br.com.fiap.nextbus.dto;

import br.com.fiap.nextbus.model.Aviso;
import br.com.fiap.nextbus.model.StatusAviso;
import br.com.fiap.nextbus.service.TextoAvisoService;

import java.time.LocalDate;

public class AvisoResponse {

    private Long id;
    private Long rotinaId;
    private LocalDate data;
    private String letreiro;
    private String nomePonto;
    private String identificadorVeiculo;
    private Integer minutos;
    private Integer idadeGpsSegundos;
    private String texto;
    private String textoCurto;
    private String comunicado;
    private StatusAviso status;

    public AvisoResponse() {
    }

    public AvisoResponse(Aviso aviso) {
        this.id = aviso.getId();
        this.rotinaId = aviso.getRotina().getId();
        this.data = aviso.getDataAviso();
        this.letreiro = aviso.getLetreiro();
        this.nomePonto = aviso.getNomePonto();
        this.identificadorVeiculo = aviso.getIdentificadorVeiculo();
        this.minutos = aviso.getMinutos();
        this.idadeGpsSegundos = aviso.getIdadeGpsSegundos();
        this.texto = aviso.getTexto();
        this.textoCurto = TextoAvisoService.textoCurto(aviso.getLetreiro(), aviso.getNomePonto(), aviso.getMinutos());
        this.status = aviso.getStatus();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getRotinaId() { return rotinaId; }
    public void setRotinaId(Long rotinaId) { this.rotinaId = rotinaId; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getLetreiro() { return letreiro; }
    public void setLetreiro(String letreiro) { this.letreiro = letreiro; }

    public String getNomePonto() { return nomePonto; }
    public void setNomePonto(String nomePonto) { this.nomePonto = nomePonto; }

    public String getIdentificadorVeiculo() { return identificadorVeiculo; }
    public void setIdentificadorVeiculo(String identificadorVeiculo) { this.identificadorVeiculo = identificadorVeiculo; }

    public Integer getMinutos() { return minutos; }
    public void setMinutos(Integer minutos) { this.minutos = minutos; }

    public Integer getIdadeGpsSegundos() { return idadeGpsSegundos; }
    public void setIdadeGpsSegundos(Integer idadeGpsSegundos) { this.idadeGpsSegundos = idadeGpsSegundos; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getTextoCurto() { return textoCurto; }
    public void setTextoCurto(String textoCurto) { this.textoCurto = textoCurto; }

    public String getComunicado() { return comunicado; }
    public void setComunicado(String comunicado) { this.comunicado = comunicado; }

    public StatusAviso getStatus() { return status; }
    public void setStatus(StatusAviso status) { this.status = status; }
}
