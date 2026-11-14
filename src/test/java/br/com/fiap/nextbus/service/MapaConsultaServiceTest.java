package br.com.fiap.nextbus.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapaConsultaServiceTest {

    private final CatalogoTransporte catalogo = new CatalogoTransporte();
    private final MapaConsultaService service = new MapaConsultaService(
            catalogo,
            new PosicaoDemonstracao(catalogo),
            new PrevisaoDemonstracao(),
            Clock.fixed(Instant.parse("2026-11-13T10:00:00Z"), ZoneId.of("America/Sao_Paulo"))
    );

    @Test
    void buscaLinhaPeloLetreiro() {
        List<CatalogoTransporte.LinhaCatalogo> linhas = service.linhas("7021");

        assertTrue(linhas.stream().anyMatch(linha -> "34831".equals(linha.codigo())));
    }

    @Test
    void paradaMaisPertoDaSeEPracaDaSe() {
        CatalogoTransporte.ParadaProxima primeira = service.paradasProximas(-23.55052, -46.63331).get(0);

        assertEquals("Praca da Se", primeira.parada().nome());
        assertEquals(0, primeira.distanciaMetros());
    }

    @Test
    void gestorVeSoALinhaComGpsVelho() {
        List<MapaConsultaService.LinhaSemGps> semGps = service.linhasSemGps();

        assertEquals(1, semGps.size());
        assertEquals("40110", semGps.get(0).codigo());
        assertTrue(semGps.get(0).idadeGpsSegundos() > 180);
    }

    @Test
    void planejaViagemDiretaDaSeAteSaoMateus() {
        List<MapaConsultaService.OpcaoViagem> opcoes = service.planejar(-23.55052, -46.63331, "Terminal Sao Mateus");

        assertEquals(1, opcoes.size());
        assertEquals("675N-10", opcoes.get(0).letreiro());
        assertEquals("Praca da Se", opcoes.get(0).nomeParada());
        assertTrue(opcoes.get(0).minutos() > 0);
        assertEquals(0, opcoes.get(0).caminhadaMetros());
    }
}
