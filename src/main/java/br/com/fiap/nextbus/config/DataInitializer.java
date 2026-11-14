package br.com.fiap.nextbus.config;

import br.com.fiap.nextbus.model.Usuario;
import br.com.fiap.nextbus.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private static final String SENHA_TESTE = "123456";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        try {
            garantirSenha("marina.costa@email.com");
            garantirSenha("ricardo.nunes@email.com");
        } catch (DataAccessException ex) {
            throw new IllegalStateException(
                    "Não foi possível ler a tabela USUARIO.",
                    ex);
        }
    }

    private void garantirSenha(String email) {
        usuarioRepository.findByEmail(email).ifPresent(this::preencherSeVazio);
    }

    private void preencherSeVazio(Usuario usuario) {
        if (usuario.getSenhaHash() == null || usuario.getSenhaHash().isBlank()) {
            usuario.setSenhaHash(passwordEncoder.encode(SENHA_TESTE));
            usuarioRepository.save(usuario);
        }
    }
}
