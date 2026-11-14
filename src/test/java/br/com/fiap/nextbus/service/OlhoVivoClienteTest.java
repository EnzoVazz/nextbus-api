package br.com.fiap.nextbus.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OlhoVivoClienteTest {

    private static final String PREVISAO = """
            {"hr":"07:06","p":{"l":[{"cl":34831,"vs":[{"p":"34518","t":"07:18","a":true,"ta":"2026-11-13T10:05:20Z"}]}]}}
            """;

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-13T10:06:00Z"), ZoneOffset.UTC);
    private final LocalDate data = LocalDate.of(2026, 11, 13);

    @Test
    void semTokenNaoChamaARede() {
        OlhoVivoCliente cliente = new OlhoVivoCliente(
                "http://127.0.0.1:1",
                " ",
                clock,
                HttpClient.newBuilder().connectTimeout(Duration.ofMillis(200)).build(),
                new OlhoVivoJson());

        assertTrue(cliente.previsao("140015892", "34831", data).isEmpty());
        assertEquals("demonstracao", cliente.situacao());
    }

    @Test
    void loginEnviaOCookieELeAPrevisao() throws IOException {
        AtomicInteger logins = new AtomicInteger();
        HttpServer server = servidor((caminho, cookie, troca) -> {
            if (caminho.startsWith("/Login/Autenticar")) {
                int numero = logins.incrementAndGet();
                return new Resposta(200, "true", "apiCredentials=c" + numero);
            }
            if (cookie != null && cookie.contains("c1")) {
                return new Resposta(200, PREVISAO, null);
            }
            return new Resposta(401, "false", null);
        });
        try {
            OlhoVivoCliente cliente = cliente(server);
            PrevisaoFeed feed = cliente.previsao("140015892", "34831", data).orElseThrow();

            assertEquals("34518", feed.veiculos().get(0).id());
            assertEquals(40, feed.veiculos().get(0).idadeGpsSegundos());
            assertEquals("ok", cliente.situacao());
            assertTrue(logins.get() >= 1);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void refazOLoginQuandoASessaoERecusada() throws IOException {
        AtomicInteger logins = new AtomicInteger();
        HttpServer server = servidor((caminho, cookie, troca) -> {
            if (caminho.startsWith("/Login/Autenticar")) {
                int numero = logins.incrementAndGet();
                return new Resposta(200, "true", "apiCredentials=c" + numero);
            }
            if (cookie != null && cookie.contains("c2")) {
                return new Resposta(200, PREVISAO, null);
            }
            return new Resposta(401, "false", null);
        });
        try {
            OlhoVivoCliente cliente = cliente(server);
            PrevisaoFeed feed = cliente.previsao("140015892", "34831", data).orElseThrow();

            assertEquals("34518", feed.veiculos().get(0).id());
            assertEquals(2, logins.get());
        } finally {
            server.stop(0);
        }
    }

    private OlhoVivoCliente cliente(HttpServer server) {
        String base = "http://127.0.0.1:" + server.getAddress().getPort();
        return new OlhoVivoCliente(base, "segredo", clock, HttpClient.newHttpClient(), new OlhoVivoJson());
    }

    private HttpServer servidor(Atendimento atendimento) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/", exchange -> {
            if ("POST".equals(exchange.getRequestMethod())
                    && exchange.getRequestHeaders().getFirst("Content-Length") == null) {
                byte[] erro = "length required".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(411, erro.length);
                try (OutputStream saida = exchange.getResponseBody()) {
                    saida.write(erro);
                }
                return;
            }
            String cookie = exchange.getRequestHeaders().getFirst("Cookie");
            Resposta resposta = atendimento.responder(exchange.getRequestURI().getPath() + "?" + exchange.getRequestURI().getQuery(), cookie, exchange.getRequestMethod());
            byte[] corpo = resposta.corpo().getBytes(StandardCharsets.UTF_8);
            if (resposta.cookie() != null) {
                exchange.getResponseHeaders().add("Set-Cookie", resposta.cookie() + "; Path=/");
            }
            exchange.sendResponseHeaders(resposta.status(), corpo.length);
            try (OutputStream saida = exchange.getResponseBody()) {
                saida.write(corpo);
            }
        });
        server.start();
        return server;
    }

    private interface Atendimento {
        Resposta responder(String caminho, String cookie, String metodo);
    }

    private record Resposta(int status, String corpo, String cookie) {
    }
}
