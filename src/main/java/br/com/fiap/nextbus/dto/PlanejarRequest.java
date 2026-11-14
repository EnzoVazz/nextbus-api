package br.com.fiap.nextbus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PlanejarRequest {

    @NotNull(message = "Informe a latitude da origem.")
    private Double origemLat;

    @NotNull(message = "Informe a longitude da origem.")
    private Double origemLng;

    @NotBlank(message = "Informe o endereço de destino.")
    private String destino;

    public Double getOrigemLat() { return origemLat; }
    public void setOrigemLat(Double origemLat) { this.origemLat = origemLat; }

    public Double getOrigemLng() { return origemLng; }
    public void setOrigemLng(Double origemLng) { this.origemLng = origemLng; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }
}
