package br.com.fiap.nextbus.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SaudeService {

    private final JdbcTemplate jdbcTemplate;
    private final OlhoVivoConsulta olhoVivo;

    public SaudeService(JdbcTemplate jdbcTemplate, OlhoVivoConsulta olhoVivo) {
        this.jdbcTemplate = jdbcTemplate;
        this.olhoVivo = olhoVivo;
    }

    public Saude verificar() {
        boolean banco = bancoResponde();
        return new Saude(banco ? "UP" : "DOWN", olhoVivo.situacao(), banco);
    }

    private boolean bancoResponde() {
        try {
            Integer um = jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            return um != null && um == 1;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public record Saude(String banco, String olhoVivo, boolean ok) {
    }
}
