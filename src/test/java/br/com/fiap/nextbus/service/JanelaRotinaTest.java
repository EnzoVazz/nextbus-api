package br.com.fiap.nextbus.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JanelaRotinaTest {

    private final JanelaRotina janela = new JanelaRotina();
    private final LocalDate sexta = LocalDate.of(2026, 11, 13);

    @Test
    void abreVinteMinutosAntesDoHorario() {
        assertFalse(janela.estaNaJanela("1,2,3,4,5", "07:15", true, sexta, LocalTime.of(6, 54)));
        assertTrue(janela.estaNaJanela("1,2,3,4,5", "07:15", true, sexta, LocalTime.of(6, 55)));
    }

    @Test
    void sabadoForaDaRotinaNaoEntra() {
        assertFalse(janela.estaNaJanela("1,2,3,4,5", "07:15", true, sexta.plusDays(1), LocalTime.of(7, 0)));
    }

    @Test
    void rotinaDesligadaNaoEntra() {
        assertFalse(janela.estaNaJanela("1,2,3,4,5", "07:15", false, sexta, LocalTime.of(7, 0)));
    }
}
