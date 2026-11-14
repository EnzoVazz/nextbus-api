package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.dto.RotinaRequest;
import br.com.fiap.nextbus.exception.IdempotenciaDivergenteException;
import br.com.fiap.nextbus.exception.RotinaDuplicadaException;
import br.com.fiap.nextbus.exception.RotinaNaoEncontradaException;
import br.com.fiap.nextbus.model.Idempotencia;
import br.com.fiap.nextbus.model.Papel;
import br.com.fiap.nextbus.model.Rotina;
import br.com.fiap.nextbus.model.Sentido;
import br.com.fiap.nextbus.model.Usuario;
import br.com.fiap.nextbus.repository.IdempotenciaRepository;
import br.com.fiap.nextbus.repository.RotinaRepository;
import br.com.fiap.nextbus.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RotinaServiceTest {

    @Mock
    private RotinaRepository rotinaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private IdempotenciaRepository idempotenciaRepository;

    @InjectMocks
    private RotinaService rotinaService;

    @AfterEach
    void limpaSessao() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void mesmaChaveDevolveARotinaSemGravarOutra() {
        entrarComoMarina();
        Idempotencia idempotencia = new Idempotencia();
        idempotencia.setIdRotina(10L);
        Rotina rotina = rotinaDaMarina();
        rotina.setId(10L);

        when(idempotenciaRepository.findByIdUsuarioAndChave(1L, "chave-1")).thenReturn(Optional.of(idempotencia));
        when(rotinaRepository.findByIdAndUsuarioId(10L, 1L)).thenReturn(Optional.of(rotina));

        RotinaService.Criacao criacao = rotinaService.criar(pedido(), "chave-1");

        assertFalse(criacao.isNova());
        assertEquals(10L, criacao.getRotina().getId());
        verify(rotinaRepository, never()).save(any());
    }

    @Test
    void naoCriaDuasRotinasAtivasIguais() {
        entrarComoMarina();
        when(idempotenciaRepository.findByIdUsuarioAndChave(1L, "chave-2")).thenReturn(Optional.empty());
        when(rotinaRepository.existeAtiva(1L, "34831", "140015892", "07:15")).thenReturn(true);

        assertThrows(RotinaDuplicadaException.class, () -> rotinaService.criar(pedido(), "chave-2"));
        verify(rotinaRepository, never()).save(any());
    }

    @Test
    void rotinaDeOutraPessoaNaoAparece() {
        entrarComoMarina();
        when(rotinaRepository.findByIdAndUsuarioId(99L, 1L)).thenReturn(Optional.empty());

        assertThrows(RotinaNaoEncontradaException.class, () -> rotinaService.buscar(99L));
    }

    @Test
    void primeiraChaveGravaARotina() {
        entrarComoMarina();
        when(idempotenciaRepository.findByIdUsuarioAndChave(1L, "chave-3")).thenReturn(Optional.empty());
        when(rotinaRepository.existeAtiva(1L, "34831", "140015892", "07:15")).thenReturn(false);
        when(rotinaRepository.save(any(Rotina.class))).thenAnswer(invocation -> {
            Rotina rotina = invocation.getArgument(0);
            rotina.setId(20L);
            return rotina;
        });

        RotinaService.Criacao criacao = rotinaService.criar(pedido(), "chave-3");

        assertTrue(criacao.isNova());
        assertEquals(20L, criacao.getRotina().getId());
        ArgumentCaptor<Idempotencia> gravada = ArgumentCaptor.forClass(Idempotencia.class);
        verify(idempotenciaRepository).save(gravada.capture());
        assertEquals(20L, gravada.getValue().getIdRotina());
        assertEquals(64, gravada.getValue().getHashCorpo().length());
        assertNotNull(gravada.getValue().getHashCorpo());
    }

    @Test
    void mesmaChaveComOutroCorpoNaoReaproveita() {
        entrarComoMarina();
        Idempotencia idempotencia = new Idempotencia();
        idempotencia.setIdRotina(10L);
        idempotencia.setHashCorpo("outro-corpo");
        when(idempotenciaRepository.findByIdUsuarioAndChave(1L, "chave-4")).thenReturn(Optional.of(idempotencia));

        assertThrows(IdempotenciaDivergenteException.class, () -> rotinaService.criar(pedido(), "chave-4"));
        verify(rotinaRepository, never()).save(any());
    }

    private void entrarComoMarina() {
        Usuario marina = new Usuario();
        marina.setId(1L);
        marina.setNome("Marina Costa");
        marina.setEmail("marina.costa@email.com");
        marina.setPapel(Papel.CIDADAO);
        when(usuarioRepository.findByEmail("marina.costa@email.com")).thenReturn(Optional.of(marina));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(marina.getEmail(), null));
    }

    private RotinaRequest pedido() {
        RotinaRequest request = new RotinaRequest();
        request.setCodigoLinha("34831");
        request.setLetreiro("7021-10");
        request.setSentido(Sentido.IDA);
        request.setCodigoParada("140015892");
        request.setNomeParada("Roberto Selmi Dei");
        request.setDiasSemana("1,2,3,4,5");
        request.setHorarioEmbarque("07:15");
        request.setAtiva(true);
        return request;
    }

    private Rotina rotinaDaMarina() {
        Rotina rotina = new Rotina();
        rotina.setCodigoLinha("34831");
        rotina.setLetreiro("7021-10");
        rotina.setSentido(Sentido.IDA);
        rotina.setCodigoParada("140015892");
        rotina.setNomeParada("Roberto Selmi Dei");
        rotina.setDiasSemana("1,2,3,4,5");
        rotina.setHorarioEmbarque("07:15");
        rotina.setAtiva(true);
        return rotina;
    }
}
