package br.com.fiap.nextbus.messaging;

import br.com.fiap.nextbus.model.StatusAviso;

public record AvisoPronto(
        Long rotinaId,
        String data,
        String letreiro,
        String nomePonto,
        String identificadorVeiculo,
        Integer minutos,
        Integer idadeGpsSegundos,
        String texto,
        StatusAviso status
) {
}
