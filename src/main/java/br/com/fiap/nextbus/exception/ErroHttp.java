package br.com.fiap.nextbus.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class ErroHttp {

    private ErroHttp() {
    }

    public static void escrever(HttpServletResponse response, int status, String erro, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String corpo = "{\"status\":" + status
                + ",\"erro\":\"" + erro
                + "\",\"mensagens\":[\"" + mensagem + "\"]}";
        response.getWriter().write(corpo);
    }
}
