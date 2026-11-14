package br.com.fiap.nextbus.service;

import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class PrevisaoDemonstracao implements PrevisaoFonte {

    static final String ONIBUS_CEDO = "34102";
    static final String ONIBUS_PERTO = "34518";
    static final String ONIBUS_TARDE = "34741";

    @Override
    public PrevisaoFeed consultar(String codigoParada, String codigoLinha, LocalDate data, LocalTime horario, Clock clock) {
        Instant base = data.atTime(horario).atZone(PrevisaoZona.ZONA).toInstant();
        Instant relogio = clock.instant();
        return new PrevisaoFeed(relogio, List.of(
                new VeiculoFeed(ONIBUS_CEDO, base.minus(Duration.ofMinutes(13)), 40),
                new VeiculoFeed(ONIBUS_PERTO, base.plus(Duration.ofMinutes(3)), 25),
                new VeiculoFeed(ONIBUS_TARDE, base.plus(Duration.ofMinutes(26)), 55)
        ));
    }
}
