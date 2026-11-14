package br.com.fiap.nextbus.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LimiteConsultaTest {

    @Test
    void aTerceiraConsultaNaMesmaJanelaERecusada() {
        LimiteConsulta limite = new LimiteConsulta(2, 60_000);

        assertTrue(limite.permitir("marina", 1_000));
        assertTrue(limite.permitir("marina", 2_000));
        assertFalse(limite.permitir("marina", 3_000));
    }

    @Test
    void depoisDaJanelaAConsultaVoltaAPassar() {
        LimiteConsulta limite = new LimiteConsulta(1, 1_000);

        assertTrue(limite.permitir("marina", 1_000));
        assertFalse(limite.permitir("marina", 1_500));
        assertTrue(limite.permitir("marina", 2_000));
    }
}
