package br.com.fiap.nextbus.service;

import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class ComunicadoCatalogo {

    private final List<Comunicado> comunicados;

    public ComunicadoCatalogo() throws IOException {
        try (InputStream entrada = getClass().getResourceAsStream("/comunicados.json")) {
            if (entrada == null) {
                this.comunicados = List.of();
                return;
            }
            this.comunicados = JsonMapper.builder().build().readValue(entrada, new TypeReference<List<Comunicado>>() {
            });
        }
    }

    public Optional<String> vigente(String codigoLinha, Instant relogio) {
        return comunicados.stream()
                .filter(comunicado -> comunicado.codigoLinha().equals(codigoLinha))
                .filter(comunicado -> !relogio.isBefore(Instant.parse(comunicado.inicio())))
                .filter(comunicado -> !relogio.isAfter(Instant.parse(comunicado.fim())))
                .map(Comunicado::texto)
                .findFirst();
    }

    public record Comunicado(String codigoLinha, String texto, String inicio, String fim) {
    }
}
