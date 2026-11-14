package br.com.fiap.nextbus.service;

public record DadosAviso(
        String letreiro,
        String nomePonto,
        Integer minutos,
        Integer idadeGpsSegundos,
        String comunicado
) {
}
