package br.com.fiap.nextbus.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class OlhoVivoCliente implements OlhoVivoConsulta {

    private static final Logger log = LoggerFactory.getLogger(OlhoVivoCliente.class);
    private static final Duration SESSAO = Duration.ofMinutes(20);
    private static final Duration ESPERA = Duration.ofSeconds(8);

    private final String baseUrl;
    private final String token;
    private final Clock clock;
    private final HttpClient http;
    private final OlhoVivoJson json;
    private final ConsultaCache<Optional<PrevisaoFeed>> cachePrevisao = new ConsultaCache<>();
    private final ConsultaCache<Optional<List<OlhoVivoJson.PosicaoBruta>>> cachePosicao = new ConsultaCache<>();
    private final ConsultaCache<String> cacheSituacao = new ConsultaCache<>();
    private final Object trava = new Object();
    private volatile String cookie;
    private volatile Instant sessaoAte;

    @Autowired
    public OlhoVivoCliente(@Value("${sptrans.base-url:https://api.olhovivo.sptrans.com.br/v2.1}") String baseUrl,
                           @Value("${sptrans.token:}") String token,
                           Clock clock) {
        this(baseUrl, token, clock, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build(), new OlhoVivoJson());
    }

    OlhoVivoCliente(String baseUrl, String token, Clock clock, HttpClient http, OlhoVivoJson json) {
        this.baseUrl = baseUrl == null ? "" : baseUrl.replaceAll("/$", "");
        this.token = token == null ? "" : token.trim();
        this.clock = clock;
        this.http = http;
        this.json = json;
    }

    @Override
    public Optional<PrevisaoFeed> previsao(String codigoParada, String codigoLinha, LocalDate data) {
        if (!temToken() || vazio(codigoParada) || vazio(codigoLinha)) {
            return Optional.empty();
        }
        String chave = "previsao:" + codigoParada + ":" + codigoLinha + ":" + data;
        return cachePrevisao.obter(chave, () -> consultarPrevisao(codigoParada, codigoLinha, data));
    }

    @Override
    public Optional<List<OlhoVivoJson.PosicaoBruta>> posicoes(String codigoLinha) {
        if (!temToken() || vazio(codigoLinha)) {
            return Optional.empty();
        }
        return cachePosicao.obter("posicao:" + codigoLinha, () -> consultarPosicoes(codigoLinha));
    }

    @Override
    public String situacao() {
        if (!temToken()) {
            return "demonstracao";
        }
        return cacheSituacao.obter("situacao", this::consultarSituacao);
    }

    private Optional<PrevisaoFeed> consultarPrevisao(String codigoParada, String codigoLinha, LocalDate data) {
        try {
            String corpo = buscar("/Previsao?codigoParada=" + codificar(codigoParada) + "&codigoLinha=" + codificar(codigoLinha));
            return Optional.of(json.previsao(corpo, codigoLinha, data, clock));
        } catch (RuntimeException ex) {
            log.warn("Olho Vivo não respondeu na previsão da linha {}", codigoLinha);
            return Optional.empty();
        }
    }

    private Optional<List<OlhoVivoJson.PosicaoBruta>> consultarPosicoes(String codigoLinha) {
        try {
            String corpo = buscar("/Posicao/Linha?codigoLinha=" + codificar(codigoLinha));
            return Optional.of(json.posicoes(corpo, LocalDate.now(clock), clock));
        } catch (RuntimeException ex) {
            log.warn("Olho Vivo não respondeu na posição da linha {}", codigoLinha);
            return Optional.empty();
        }
    }

    private String consultarSituacao() {
        synchronized (trava) {
            return login() ? "ok" : "indisponivel";
        }
    }

    private String buscar(String caminho) {
        for (int tentativa = 0; tentativa < 2; tentativa++) {
            if (!garantirSessao()) {
                throw new IllegalStateException("sem sessão");
            }
            HttpResponse<String> resposta = enviar(pedido(uri(caminho)).GET().build());
            if (sessaoNegada(resposta) && tentativa == 0) {
                invalidar();
                continue;
            }
            if (resposta.statusCode() >= 200 && resposta.statusCode() < 300 && !sessaoNegada(resposta)) {
                return resposta.body();
            }
            throw new IllegalStateException("resposta " + resposta.statusCode());
        }
        throw new IllegalStateException("sem sessão");
    }

    private boolean garantirSessao() {
        if (sessaoValida()) {
            return true;
        }
        synchronized (trava) {
            if (sessaoValida()) {
                return true;
            }
            return login();
        }
    }

    private boolean login() {
        try {
            String caminho = "/Login/Autenticar?token=" + codificar(token);
            HttpResponse<String> resposta = enviar(pedido(uri(caminho))
                    .POST(HttpRequest.BodyPublishers.ofString(" "))
                    .build());
            if (!corpoVerdadeiro(resposta.body()) || resposta.statusCode() < 200 || resposta.statusCode() >= 300) {
                log.warn("Login da Olho Vivo recusado, status {}", resposta.statusCode());
                return false;
            }
            String novoCookie = lerCookie(resposta);
            if (novoCookie == null) {
                log.warn("Login da Olho Vivo não devolveu o cookie da sessão");
                return false;
            }
            cookie = novoCookie;
            sessaoAte = clock.instant().plus(SESSAO);
            return true;
        } catch (RuntimeException ex) {
            log.warn("Falha ao autenticar na Olho Vivo");
            return false;
        }
    }

    private void invalidar() {
        cookie = null;
        sessaoAte = null;
    }

    private boolean sessaoValida() {
        return cookie != null && sessaoAte != null && clock.instant().isBefore(sessaoAte);
    }

    private HttpResponse<String> enviar(HttpRequest pedido) {
        try {
            return http.send(pedido, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("consulta interrompida");
        } catch (IOException ex) {
            throw new IllegalStateException("falha de rede");
        }
    }

    private HttpRequest.Builder pedido(URI uri) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(ESPERA)
                .header("Accept", "application/json")
                .header("User-Agent", "nextbus");
        if (cookie != null) {
            builder.header("Cookie", cookie);
        }
        return builder;
    }

    private URI uri(String caminho) {
        return URI.create(baseUrl + caminho);
    }

    private static boolean sessaoNegada(HttpResponse<String> resposta) {
        int status = resposta.statusCode();
        if (status == 401 || status == 403) {
            return true;
        }
        String corpo = resposta.body() == null ? "" : resposta.body();
        return corpo.contains("Authorization has been denied") || "false".equalsIgnoreCase(corpo.trim());
    }

    private static boolean corpoVerdadeiro(String corpo) {
        return corpo != null && "true".equalsIgnoreCase(corpo.trim().replace("\uFEFF", ""));
    }

    private static String lerCookie(HttpResponse<String> resposta) {
        List<String> cookies = resposta.headers().allValues("Set-Cookie");
        if (cookies.isEmpty()) {
            return null;
        }
        StringBuilder junta = new StringBuilder();
        for (String valor : cookies) {
            String par = valor.split(";", 2)[0].trim();
            if (par.isEmpty()) {
                continue;
            }
            if (!junta.isEmpty()) {
                junta.append("; ");
            }
            junta.append(par);
        }
        return junta.isEmpty() ? null : junta.toString();
    }

    private boolean temToken() {
        return token != null && !token.isBlank();
    }

    private static boolean vazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(valor, StandardCharsets.UTF_8);
    }
}
