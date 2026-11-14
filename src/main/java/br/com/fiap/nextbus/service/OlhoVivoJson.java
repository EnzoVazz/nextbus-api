package br.com.fiap.nextbus.service;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class OlhoVivoJson {

    private final JsonMapper mapper = JsonMapper.builder().build();

    PrevisaoFeed previsao(String json, String codigoLinha, LocalDate data, Clock clock) {
        JsonNode raiz = ler(json);
        Instant relogio = relogio(texto(raiz.path("hr")), data);
        List<VeiculoFeed> veiculos = new ArrayList<>();
        JsonNode linhas = raiz.path("p").path("l");
        if (linhas.isArray()) {
            for (int i = 0; i < linhas.size(); i++) {
                JsonNode linha = linhas.get(i);
                if (!mesmaLinha(linha, codigoLinha)) {
                    continue;
                }
                adicionarPrevistos(linha.path("vs"), data, relogio, clock, veiculos);
            }
        }
        return new PrevisaoFeed(relogio, List.copyOf(veiculos));
    }

    List<PosicaoBruta> posicoes(String json, LocalDate data, Clock clock) {
        JsonNode raiz = ler(json);
        Instant relogio = relogio(texto(raiz.path("hr")), data);
        List<PosicaoBruta> veiculos = new ArrayList<>();
        JsonNode lista = raiz.path("vs");
        if (!lista.isArray()) {
            return List.of();
        }
        for (int i = 0; i < lista.size(); i++) {
            JsonNode veiculo = lista.get(i);
            String id = texto(veiculo.path("p"));
            if (id == null || !veiculo.path("py").isNumber() || !veiculo.path("px").isNumber()) {
                continue;
            }
            veiculos.add(new PosicaoBruta(
                    id,
                    veiculo.path("py").asDouble(),
                    veiculo.path("px").asDouble(),
                    idade(texto(veiculo.path("ta")), relogio, clock.instant()),
                    veiculo.path("a").isBoolean() && veiculo.path("a").asBoolean()
            ));
        }
        return List.copyOf(veiculos);
    }

    private void adicionarPrevistos(JsonNode lista, LocalDate data, Instant relogio, Clock clock, List<VeiculoFeed> destino) {
        if (!lista.isArray()) {
            return;
        }
        LocalTime horaFeed = LocalTime.ofInstant(relogio, PrevisaoZona.ZONA);
        for (int i = 0; i < lista.size(); i++) {
            JsonNode veiculo = lista.get(i);
            String id = texto(veiculo.path("p"));
            LocalTime chegada = hora(texto(veiculo.path("t")));
            if (id == null || chegada == null) {
                continue;
            }
            destino.add(new VeiculoFeed(
                    id,
                    chegadaNoDia(data, horaFeed, chegada),
                    idade(texto(veiculo.path("ta")), relogio, clock.instant())
            ));
        }
    }

    private JsonNode ler(String json) {
        try {
            JsonNode raiz = mapper.readTree(json);
            if (texto(raiz.path("hr")) == null) {
                throw new IllegalArgumentException("A Olho Vivo não devolveu o relógio.");
            }
            return raiz;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("A Olho Vivo devolveu uma resposta inválida.");
        }
    }

    private static boolean mesmaLinha(JsonNode linha, String codigoLinha) {
        String codigo = texto(linha.path("cl"));
        return codigo == null || codigo.equals(codigoLinha);
    }

    private static Instant relogio(String hora, LocalDate data) {
        LocalTime lida = hora(hora);
        if (lida == null) {
            throw new IllegalArgumentException("A Olho Vivo não devolveu o relógio.");
        }
        return data.atTime(lida).atZone(PrevisaoZona.ZONA).toInstant();
    }

    static Instant chegadaNoDia(LocalDate data, LocalTime relogio, LocalTime prevista) {
        Instant base = data.atTime(relogio).atZone(PrevisaoZona.ZONA).toInstant();
        Instant alvo = data.atTime(prevista).atZone(PrevisaoZona.ZONA).toInstant();
        if (alvo.isBefore(base) && Duration.between(alvo, base).toHours() >= 12) {
            alvo = alvo.plus(Duration.ofDays(1));
        }
        return alvo;
    }

    static int idade(String captura, Instant relogio, Instant agora) {
        Instant referencia = instante(captura);
        if (referencia == null) {
            referencia = relogio;
        }
        long segundos = Duration.between(referencia, agora).getSeconds();
        if (segundos < 0) {
            return 0;
        }
        return segundos > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) segundos;
    }

    private static Instant instante(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return Instant.parse(texto);
        } catch (DateTimeParseException ignorado) {
            try {
                return OffsetDateTime.parse(texto).toInstant();
            } catch (DateTimeParseException tambem) {
                try {
                    return LocalDateTime.parse(texto).atZone(PrevisaoZona.ZONA).toInstant();
                } catch (DateTimeParseException ultimo) {
                    return null;
                }
            }
        }
    }

    private static LocalTime hora(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return LocalTime.parse(texto);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private static String texto(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return null;
        }
        String valor = node.asString().trim();
        if (valor.endsWith(".0")) {
            valor = valor.substring(0, valor.length() - 2);
        }
        return valor.isEmpty() ? null : valor;
    }

    public record PosicaoBruta(String id, double lat, double lng, int idadeGpsSegundos, boolean acessivel) {
    }
}
