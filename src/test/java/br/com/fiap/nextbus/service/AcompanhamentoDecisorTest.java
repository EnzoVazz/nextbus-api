package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.model.StatusAviso;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcompanhamentoDecisorTest {

    private static final ZoneId ZONA = ZoneId.of("America/Sao_Paulo");
    private static final LocalDate DIA = LocalDate.of(2026, 11, 13);
    private final AcompanhamentoDecisor decisor = new AcompanhamentoDecisor();

    @Test
    void rotinaDasSeteEQuinzeSegueOOnibusDasSeteEDezoito() {
        Optional<AcompanhamentoDecisor.Decisao> decisao = decisor.decidir(entrada(hora(6, 58), null, null, null));

        assertTrue(decisao.isPresent());
        assertFalse(decisao.get().persistir());
        assertEquals(PrevisaoDemonstracao.ONIBUS_PERTO, decisao.get().memorarVeiculo());
    }

    @Test
    void primeiraNotificacaoSaiComDozeMinutos() {
        AcompanhamentoDecisor.Decisao decisao = decisor.decidir(
                entrada(hora(7, 6), PrevisaoDemonstracao.ONIBUS_PERTO, null, null)).orElseThrow();

        assertTrue(decisao.persistir());
        assertEquals(StatusAviso.ENVIADO, decisao.status());
        assertEquals(PrevisaoDemonstracao.ONIBUS_PERTO, decisao.veiculoId());
        assertEquals(12, decisao.minutos());
    }

    @Test
    void atualizaAoCruzarDezENaoTrocaDeOnibus() {
        AcompanhamentoDecisor.Decisao decisao = decisor.decidir(
                entrada(hora(7, 10), PrevisaoDemonstracao.ONIBUS_PERTO, 12, StatusAviso.ENVIADO)).orElseThrow();

        assertEquals(StatusAviso.ATUALIZADO, decisao.status());
        assertEquals(8, decisao.minutos());
        assertEquals(PrevisaoDemonstracao.ONIBUS_PERTO, decisao.veiculoId());
    }

    @Test
    void entreDezECincoNaoNasceOutroAviso() {
        AcompanhamentoDecisor.Decisao decisao = decisor.decidir(
                entrada(hora(7, 9), PrevisaoDemonstracao.ONIBUS_PERTO, 8, StatusAviso.ATUALIZADO)).orElseThrow();

        assertFalse(decisao.persistir());
    }

    @Test
    void aoChegarEmZeroEncerraOMesmoAviso() {
        AcompanhamentoDecisor.Decisao decisao = decisor.decidir(
                entrada(hora(7, 19), PrevisaoDemonstracao.ONIBUS_PERTO, 2, StatusAviso.ATUALIZADO)).orElseThrow();

        assertEquals(StatusAviso.ENCERRADO, decisao.status());
        assertEquals(0, decisao.minutos());
        assertEquals(PrevisaoDemonstracao.ONIBUS_PERTO, decisao.veiculoId());
    }

    @Test
    void semVeiculoAvisaUmaVez() {
        AcompanhamentoDecisor.Entrada semNinguem = new AcompanhamentoDecisor.Entrada(
                horario(), hora(7, 0), List.of(), null, null, null);

        AcompanhamentoDecisor.Decisao decisao = decisor.decidir(semNinguem).orElseThrow();

        assertEquals(StatusAviso.SEM_PREVISAO, decisao.status());
        assertEquals(null, decisao.veiculoId());
        assertEquals(null, decisao.minutos());
    }

    private AcompanhamentoDecisor.Entrada entrada(Instant relogio, String preso, Integer minutos, StatusAviso status) {
        PrevisaoFeed feed = new PrevisaoDemonstracao().consultar("140015892", "34831", DIA, LocalTime.of(7, 15),
                java.time.Clock.fixed(relogio, ZONA));
        return new AcompanhamentoDecisor.Entrada(horario(), feed.relogio(), feed.veiculos(), preso, minutos, status);
    }

    private Instant horario() {
        return ZonedDateTime.of(DIA, LocalTime.of(7, 15), ZONA).toInstant();
    }

    private Instant hora(int hora, int minuto) {
        return ZonedDateTime.of(DIA, LocalTime.of(hora, minuto), ZONA).toInstant();
    }
}
