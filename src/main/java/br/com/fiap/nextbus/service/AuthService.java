package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.dto.LoginRequest;
import br.com.fiap.nextbus.dto.RegistrarRequest;
import br.com.fiap.nextbus.dto.TokenResponse;
import br.com.fiap.nextbus.dto.UsuarioResponse;
import br.com.fiap.nextbus.exception.EmailJaCadastradoException;
import br.com.fiap.nextbus.model.Papel;
import br.com.fiap.nextbus.model.Usuario;
import br.com.fiap.nextbus.repository.UsuarioRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public UsuarioResponse registrar(RegistrarRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new EmailJaCadastradoException();
        }

        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome().trim());
        usuario.setEmail(email);
        usuario.setPapel(Papel.CIDADAO);
        usuario.setSenhaHash(passwordEncoder.encode(request.getSenha()));

        Usuario salvo = usuarioRepository.save(usuario);
        return paraResposta(salvo);
    }

    public TokenResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getSenha()));

        Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
        String token = jwtService.gerar(usuario);
        return new TokenResponse(token, usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPapel());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse eu() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByEmail(autenticacao.getName()).orElseThrow();
        return paraResposta(usuario);
    }

    private UsuarioResponse paraResposta(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPapel());
    }
}
