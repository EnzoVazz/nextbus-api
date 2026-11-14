package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.exception.ErroHttp;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LimiteFiltro extends OncePerRequestFilter {

    private final LimiteConsulta limite = new LimiteConsulta(60, 60_000);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (HttpMethod.GET.matches(request.getMethod()) && leitura(request.getRequestURI())) {
            if (!limite.permitir(chave(request), System.currentTimeMillis())) {
                ErroHttp.escrever(response, 429, "limite", "Muitas consultas. Tente de novo em instantes.");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean leitura(String uri) {
        return uri.startsWith("/linhas") || uri.startsWith("/paradas") || uri.startsWith("/previsao");
    }

    private String chave(HttpServletRequest request) {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null && autenticacao.isAuthenticated() && autenticacao.getName() != null) {
            return autenticacao.getName();
        }
        return request.getRemoteAddr();
    }
}
