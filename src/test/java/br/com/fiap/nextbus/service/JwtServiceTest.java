package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.model.Papel;
import br.com.fiap.nextbus.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    @Test
    void tokenGuardaOEmailDoUsuario() {
        JwtService jwtService = new JwtService("nextbus-gs-2026-chave-local-de-teste", 60);
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Marina Costa");
        usuario.setEmail("marina.costa@email.com");
        usuario.setPapel(Papel.CIDADAO);

        String token = jwtService.gerar(usuario);

        assertEquals("marina.costa@email.com", jwtService.extrairEmail(token));
        assertTrue(jwtService.tokenValido(token, new User(usuario.getEmail(), "hash", List.of())));
    }
}
