package br.com.fiap.nextbus.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

@Component
@Primary
public class PrevisaoOlhoVivo implements PrevisaoFonte {

    private final OlhoVivoConsulta olhoVivo;
    private final PrevisaoDemonstracao demonstracao;

    public PrevisaoOlhoVivo(OlhoVivoConsulta olhoVivo, PrevisaoDemonstracao demonstracao) {
        this.olhoVivo = olhoVivo;
        this.demonstracao = demonstracao;
    }

    @Override
    public PrevisaoFeed consultar(String codigoParada, String codigoLinha, LocalDate data, LocalTime horario, Clock clock) {
        return olhoVivo.previsao(codigoParada, codigoLinha, data)
                .orElseGet(() -> demonstracao.consultar(codigoParada, codigoLinha, data, horario, clock));
    }
}
