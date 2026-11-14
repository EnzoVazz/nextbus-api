package br.com.fiap.nextbus.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PrevisaoOlhoVivoTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-13T10:00:00Z"), ZoneOffset.UTC);
    private final LocalDate data = LocalDate.of(2026, 11, 13);
    private final PrevisaoDemonstracao demonstracao = new PrevisaoDemonstracao();

    @Test
    void usaDemonstracaoQuandoAOlhoVivoNaoResponde() {
        PrevisaoFeed feed = new PrevisaoOlhoVivo(consulta(Optional.empty()), demonstracao)
                .consultar("140015892", "34831", data, LocalTime.of(7, 15), clock);

        assertEquals(3, feed.veiculos().size());
        assertEquals(PrevisaoDemonstracao.ONIBUS_PERTO, feed.veiculos().get(1).id());
    }

    @Test
    void mantemARespostaRealMesmoSemOnibus() {
        PrevisaoFeed vazio = new PrevisaoFeed(clock.instant(), List.of());
        PrevisaoFeed feed = new PrevisaoOlhoVivo(consulta(Optional.of(vazio)), demonstracao)
                .consultar("140015892", "34831", data, LocalTime.of(7, 15), clock);

        assertTrue(feed.veiculos().isEmpty());
        assertEquals(clock.instant(), feed.relogio());
    }

    private static OlhoVivoConsulta consulta(Optional<PrevisaoFeed> feed) {
        return new OlhoVivoConsulta() {
            @Override
            public Optional<PrevisaoFeed> previsao(String codigoParada, String codigoLinha, LocalDate data) {
                return feed;
            }

            @Override
            public Optional<List<OlhoVivoJson.PosicaoBruta>> posicoes(String codigoLinha) {
                return Optional.empty();
            }

            @Override
            public String situacao() {
                return "ok";
            }
        };
    }
}
