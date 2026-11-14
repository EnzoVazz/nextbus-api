package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.dto.PaginaResponse;
import br.com.fiap.nextbus.dto.RotinaRequest;
import br.com.fiap.nextbus.dto.RotinaResponse;
import br.com.fiap.nextbus.exception.IdempotenciaDivergenteException;
import br.com.fiap.nextbus.exception.RequisicaoInvalidaException;
import br.com.fiap.nextbus.exception.RotinaComAvisoException;
import br.com.fiap.nextbus.exception.RotinaDuplicadaException;
import br.com.fiap.nextbus.exception.RotinaNaoEncontradaException;
import br.com.fiap.nextbus.model.Idempotencia;
import br.com.fiap.nextbus.model.Rotina;
import br.com.fiap.nextbus.model.Usuario;
import br.com.fiap.nextbus.repository.IdempotenciaRepository;
import br.com.fiap.nextbus.repository.RotinaRepository;
import br.com.fiap.nextbus.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class RotinaService {

    private final RotinaRepository rotinaRepository;
    private final UsuarioRepository usuarioRepository;
    private final IdempotenciaRepository idempotenciaRepository;

    public RotinaService(RotinaRepository rotinaRepository,
                         UsuarioRepository usuarioRepository,
                         IdempotenciaRepository idempotenciaRepository) {
        this.rotinaRepository = rotinaRepository;
        this.usuarioRepository = usuarioRepository;
        this.idempotenciaRepository = idempotenciaRepository;
    }

    @Transactional(readOnly = true)
    public PaginaResponse<RotinaResponse> listar(Pageable pageable) {
        Usuario usuario = usuarioLogado();
        int tamanho = Math.min(Math.max(pageable.getPageSize(), 1), 50);
        Sort ordem = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by("horarioEmbarque");
        Pageable limitado = PageRequest.of(Math.max(pageable.getPageNumber(), 0), tamanho, ordem);
        Page<RotinaResponse> pagina = rotinaRepository.findByUsuarioId(usuario.getId(), limitado)
                .map(RotinaResponse::new);
        return new PaginaResponse<>(pagina);
    }

    @Transactional(readOnly = true)
    public RotinaResponse buscar(Long id) {
        return new RotinaResponse(buscarDoUsuario(usuarioLogado(), id));
    }

    @Transactional
    public Criacao criar(RotinaRequest request, String chave) {
        String chaveLimpa = validarChave(chave);
        Usuario usuario = usuarioLogado();
        boolean ativa = request.getAtiva() == null || request.getAtiva();
        String hash = hashCorpo(request, ativa);

        var anterior = idempotenciaRepository.findByIdUsuarioAndChave(usuario.getId(), chaveLimpa);
        if (anterior.isPresent()) {
            String hashGravado = anterior.get().getHashCorpo();
            if (hashGravado != null && !hashGravado.equals(hash)) {
                throw new IdempotenciaDivergenteException();
            }
            Rotina existente = buscarDoUsuario(usuario, anterior.get().getIdRotina());
            return new Criacao(new RotinaResponse(existente), false);
        }

        if (ativa && rotinaRepository.existeAtiva(usuario.getId(), request.getCodigoLinha().trim(),
                request.getCodigoParada().trim(), request.getHorarioEmbarque().trim())) {
            throw new RotinaDuplicadaException();
        }

        Rotina rotina = new Rotina();
        rotina.setUsuario(usuario);
        preencher(rotina, request, ativa);
        Rotina salva = rotinaRepository.save(rotina);

        Idempotencia idempotencia = new Idempotencia();
        idempotencia.setIdUsuario(usuario.getId());
        idempotencia.setChave(chaveLimpa);
        idempotencia.setHashCorpo(hash);
        idempotencia.setIdRotina(salva.getId());
        idempotenciaRepository.save(idempotencia);

        return new Criacao(new RotinaResponse(salva), true);
    }

    @Transactional
    public RotinaResponse atualizar(Long id, RotinaRequest request) {
        Usuario usuario = usuarioLogado();
        Rotina rotina = buscarDoUsuario(usuario, id);
        boolean ativa = request.getAtiva() == null ? Boolean.TRUE.equals(rotina.getAtiva()) : request.getAtiva();
        if (ativa && rotinaRepository.existeOutraAtiva(usuario.getId(), request.getCodigoLinha().trim(),
                request.getCodigoParada().trim(), request.getHorarioEmbarque().trim(), rotina.getId())) {
            throw new RotinaDuplicadaException();
        }
        preencher(rotina, request, ativa);
        return new RotinaResponse(rotinaRepository.save(rotina));
    }

    @Transactional
    public void excluir(Long id) {
        Usuario usuario = usuarioLogado();
        Rotina rotina = buscarDoUsuario(usuario, id);
        idempotenciaRepository.deleteByIdRotina(rotina.getId());
        try {
            rotinaRepository.delete(rotina);
            rotinaRepository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new RotinaComAvisoException();
        }
    }

    private Rotina buscarDoUsuario(Usuario usuario, Long id) {
        return rotinaRepository.findByIdAndUsuarioId(id, usuario.getId())
                .orElseThrow(RotinaNaoEncontradaException::new);
    }

    private void preencher(Rotina rotina, RotinaRequest request, boolean ativa) {
        rotina.setCodigoLinha(request.getCodigoLinha().trim());
        rotina.setLetreiro(request.getLetreiro().trim());
        rotina.setSentido(request.getSentido());
        rotina.setCodigoParada(request.getCodigoParada().trim());
        rotina.setNomeParada(request.getNomeParada().trim());
        rotina.setDiasSemana(request.getDiasSemana().trim());
        rotina.setHorarioEmbarque(request.getHorarioEmbarque().trim());
        rotina.setAtiva(ativa);
    }

    private String hashCorpo(RotinaRequest request, boolean ativa) {
        String canonico = String.join("\n",
                request.getCodigoLinha().trim(),
                request.getLetreiro().trim(),
                request.getSentido().name(),
                request.getCodigoParada().trim(),
                request.getNomeParada().trim(),
                request.getDiasSemana().trim(),
                request.getHorarioEmbarque().trim(),
                ativa ? "S" : "N");
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonico.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte parte : digest) {
                hex.append(String.format("%02x", parte));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Não foi possível calcular o hash da rotina.", ex);
        }
    }

    private String validarChave(String chave) {
        if (chave == null || chave.isBlank()) {
            throw new RequisicaoInvalidaException("Informe o cabeçalho Idempotency-Key.");
        }
        String limpa = chave.trim();
        if (limpa.length() > 80) {
            throw new RequisicaoInvalidaException("A Idempotency-Key pode ter no máximo 80 caracteres.");
        }
        return limpa;
    }

    private Usuario usuarioLogado() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByEmail(autenticacao.getName()).orElseThrow(RotinaNaoEncontradaException::new);
    }

    public static final class Criacao {
        private final RotinaResponse rotina;
        private final boolean nova;

        public Criacao(RotinaResponse rotina, boolean nova) {
            this.rotina = rotina;
            this.nova = nova;
        }

        public RotinaResponse getRotina() { return rotina; }
        public boolean isNova() { return nova; }
    }
}
