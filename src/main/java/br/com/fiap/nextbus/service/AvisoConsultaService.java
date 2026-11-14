package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.dto.AvisoResponse;
import br.com.fiap.nextbus.model.Aviso;
import br.com.fiap.nextbus.model.Usuario;
import br.com.fiap.nextbus.repository.AvisoRepository;
import br.com.fiap.nextbus.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class AvisoConsultaService {

    private final AvisoRepository avisoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComunicadoCatalogo comunicados;
    private final Clock clock;

    public AvisoConsultaService(AvisoRepository avisoRepository,
                                UsuarioRepository usuarioRepository,
                                ComunicadoCatalogo comunicados,
                                Clock clock) {
        this.avisoRepository = avisoRepository;
        this.usuarioRepository = usuarioRepository;
        this.comunicados = comunicados;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<AvisoResponse> doDia() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByEmail(autenticacao.getName()).orElseThrow();
        return avisoRepository.findDoUsuarioNoDia(usuario.getId(), LocalDate.now(clock)).stream()
                .map(this::resposta)
                .toList();
    }

    private AvisoResponse resposta(Aviso aviso) {
        AvisoResponse resposta = new AvisoResponse(aviso);
        String codigoLinha = aviso.getRotina().getCodigoLinha();
        resposta.setComunicado(comunicados.vigente(codigoLinha, clock.instant()).orElse(null));
        return resposta;
    }
}
