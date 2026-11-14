package br.com.fiap.nextbus.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OlhoVivoJsonTest {

    private final OlhoVivoJson json = new OlhoVivoJson();
    private final LocalDate data = LocalDate.of(2026, 11, 13);
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-13T10:06:00Z"), ZoneOffset.UTC);

    @Test
    void previsaoUsaPrefixoRelogioEChegada() {
        String corpo = """
                {"hr":"07:06","p":{"cp":140015892,"np":"ROBERTO SELMI DEI","l":[
                  {"cl":34831,"vs":[{"p":"34518","t":"07:18","a":true,"ta":"2026-11-13T10:05:20Z"}]}
                ]}}
                """;

        PrevisaoFeed feed = json.previsao(corpo, "34831", data, clock);

        assertEquals(Instant.parse("2026-11-13T10:06:00Z"), feed.relogio());
        assertEquals(1, feed.veiculos().size());
        assertEquals("34518", feed.veiculos().get(0).id());
        assertEquals(Instant.parse("2026-11-13T10:18:00Z"), feed.veiculos().get(0).chegada());
        assertEquals(40, feed.veiculos().get(0).idadeGpsSegundos());
    }

    @Test
    void prefixoNumericoViraTexto() {
        String corpo = """
                {"hr":"07:06","p":{"l":[{"cl":34831,"vs":[{"p":74558,"t":"07:18","a":true}]}]}}
                """;

        PrevisaoFeed feed = json.previsao(corpo, "34831", data, clock);

        assertEquals("74558", feed.veiculos().get(0).id());
    }

    @Test
    void ignoraOutraLinhaEListaVazia() {
        String corpo = """
                {"hr":"07:06","p":{"l":[{"cl":999,"vs":[{"p":"1","t":"07:10"}]}]}}
                """;

        PrevisaoFeed feed = json.previsao(corpo, "34831", data, clock);

        assertTrue(feed.veiculos().isEmpty());
    }

    @Test
    void chegadaDepoisDaMeiaNoiteAvancaODia() {
        Instant chegada = OlhoVivoJson.chegadaNoDia(data, java.time.LocalTime.of(23, 50), java.time.LocalTime.of(0, 10));

        assertEquals(Instant.parse("2026-11-14T03:10:00Z"), chegada);
    }

    @Test
    void chegadaQueJaPassouNaoVaiParaODiaSeguinte() {
        Instant chegada = OlhoVivoJson.chegadaNoDia(data, java.time.LocalTime.of(8, 0), java.time.LocalTime.of(7, 30));

        assertEquals(Instant.parse("2026-11-13T10:30:00Z"), chegada);
    }

    @Test
    void posicaoLePrefixoCoordenadaEIdade() {
        String corpo = """
                {"hr":"07:06","vs":[{"p":68021,"a":true,"ta":"2026-11-13T10:05:20Z","py":-23.502,"px":-46.71}]}
                """;

        List<OlhoVivoJson.PosicaoBruta> veiculos = json.posicoes(corpo, data, clock);

        assertEquals(1, veiculos.size());
        assertEquals("68021", veiculos.get(0).id());
        assertEquals(-23.502, veiculos.get(0).lat());
        assertEquals(-46.71, veiculos.get(0).lng());
        assertEquals(40, veiculos.get(0).idadeGpsSegundos());
        assertTrue(veiculos.get(0).acessivel());
    }

    @Test
    void respostaSemRelogioFalha() {
        assertThrows(IllegalArgumentException.class, () -> json.previsao("{}", "34831", data, clock));
    }
}
