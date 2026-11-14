package br.com.fiap.nextbus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextoAvisoServiceTest {

    @Test
    void gpsNovoComMinutosEntraNaFrase() {
        TextoAvisoService service = new TextoAvisoService(instrucao -> instrucao);
        DadosAviso dados = new DadosAviso("7021-10", "Roberto Selmi Dei", 12, 25, null);

        String frase = service.gerar(dados);

        assertTrue(frase.contains("7021-10"));
        assertTrue(frase.contains("Roberto Selmi Dei"));
        assertTrue(frase.contains("12 minutos"));
        assertTrue(frase.contains("25 segundos"));
    }

    @Test
    void linhaSemVeiculoNaoInventaOcorrencia() {
        TextoAvisoService service = new TextoAvisoService(instrucao -> {
            throw new RuntimeException("modelo indisponível");
        });
        DadosAviso dados = new DadosAviso("7021-10", "Roberto Selmi Dei", null, null, null);

        String frase = service.gerar(dados);

        assertEquals("7021-10 sem previsão no seu ponto agora.", frase);
        assertFalse(frase.toLowerCase().contains("acidente"));
        assertFalse(frase.toLowerCase().contains("desvio"));
        assertFalse(frase.toLowerCase().contains("lotação"));
        assertFalse(frase.toLowerCase().contains("lotacao"));
    }

    @Test
    void respostaEmFormatoDeFichaUsaAFrasePadrao() {
        TextoAvisoService service = new TextoAvisoService(instrucao ->
                "7021-10 — Roberto Selmi Dei\nSem previsão — Idade do GPS: não informado");
        DadosAviso dados = new DadosAviso(
                "7021-10",
                "Roberto Selmi Dei",
                null,
                null,
                "A linha opera com desvio na região da Lapa."
        );

        String frase = service.gerar(dados);

        assertEquals(
                "7021-10 sem previsão no seu ponto agora. Comunicado: A linha opera com desvio na região da Lapa.",
                frase
        );
    }

    @Test
    void textoCurtoEApenasANotificacao() {
        assertEquals(
                "7021-10 no ponto Roberto Selmi Dei: 12 minutos",
                TextoAvisoService.textoCurto("7021-10", "Roberto Selmi Dei", 12)
        );
        assertEquals(
                "7021-10 sem previsão no seu ponto agora.",
                TextoAvisoService.textoCurto("7021-10", "Roberto Selmi Dei", null)
        );
    }

    @Test
    void comunicadoVigenteApareceMesmoSemModelo() {
        TextoAvisoService service = new TextoAvisoService(instrucao -> {
            throw new RuntimeException("modelo indisponível");
        });
        DadosAviso dados = new DadosAviso(
                "7021-10",
                "Roberto Selmi Dei",
                12,
                25,
                "A linha opera com desvio na região da Lapa."
        );

        String frase = service.gerar(dados);

        assertTrue(frase.contains("12 minutos"));
        assertTrue(frase.contains("A linha opera com desvio na região da Lapa."));
    }
}
