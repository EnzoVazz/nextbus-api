package br.com.fiap.nextbus.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MapeamentoOracleTest {

    @Test
    void ativaGravaSOuN() {
        SimNaoConverter converter = new SimNaoConverter();

        assertEquals("S", converter.convertToDatabaseColumn(true));
        assertEquals("N", converter.convertToDatabaseColumn(false));
        assertTrue(converter.convertToEntityAttribute("S"));
        assertFalse(converter.convertToEntityAttribute("N"));
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void sentidoGrava1Ou2() {
        SentidoConverter converter = new SentidoConverter();

        assertEquals(1, converter.convertToDatabaseColumn(Sentido.IDA));
        assertEquals(2, converter.convertToDatabaseColumn(Sentido.VOLTA));
        assertEquals(Sentido.IDA, converter.convertToEntityAttribute(1));
        assertEquals(Sentido.VOLTA, converter.convertToEntityAttribute(2));
    }

    @Test
    void horarioCharVemSemEspaco() {
        Rotina rotina = new Rotina();
        rotina.setHorarioEmbarque("07:15 ");

        assertEquals("07:15", rotina.getHorarioEmbarque());
    }

    @Test
    void hashDaCargaConfereComASenhaDeTeste() throws Exception {
        String sql = Files.readString(Path.of("db/002-senha-hash.sql"));
        int inicio = sql.indexOf("'$2");
        int fim = sql.indexOf('\'', inicio + 1);
        String hash = sql.substring(inicio + 1, fim);

        assertTrue(new BCryptPasswordEncoder().matches("123456", hash));
    }
}
