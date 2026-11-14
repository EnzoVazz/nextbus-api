package br.com.fiap.nextbus.dto;

import br.com.fiap.nextbus.model.Sentido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RotinaRequest {

    @NotBlank(message = "Informe o código da linha.")
    @Size(max = 20, message = "O código da linha pode ter no máximo 20 caracteres.")
    private String codigoLinha;

    @NotBlank(message = "Informe o letreiro.")
    @Size(max = 20, message = "O letreiro pode ter no máximo 20 caracteres.")
    private String letreiro;

    @NotNull(message = "Informe o sentido: IDA ou VOLTA.")
    private Sentido sentido;

    @NotBlank(message = "Informe o código do ponto.")
    @Size(max = 20, message = "O código do ponto pode ter no máximo 20 caracteres.")
    private String codigoParada;

    @NotBlank(message = "Informe o nome do ponto.")
    @Size(max = 160, message = "O nome do ponto pode ter no máximo 160 caracteres.")
    private String nomeParada;

    @NotBlank(message = "Informe os dias da semana. Use 1 para segunda até 7 para domingo.")
    @Pattern(regexp = "^[1-7](,[1-7])*$", message = "Os dias devem ser números de 1 a 7, separados por vírgula.")
    private String diasSemana;

    @NotBlank(message = "Informe o horário de embarque.")
    @Pattern(regexp = "^([01][0-9]|2[0-3]):[0-5][0-9]$", message = "O horário deve estar no formato HH:mm.")
    private String horarioEmbarque;

    private Boolean ativa;

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
