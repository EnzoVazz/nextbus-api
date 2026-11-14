package br.com.fiap.nextbus.controller;

import br.com.fiap.nextbus.dto.PaginaResponse;
import br.com.fiap.nextbus.dto.RotinaRequest;
import br.com.fiap.nextbus.dto.RotinaResponse;
import br.com.fiap.nextbus.service.RotinaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rotinas")
@Tag(name = "2. Rotinas", description = "Rotinas de embarque do cidadão autenticado")
public class RotinaController {

    private final RotinaService rotinaService;

    public RotinaController(RotinaService rotinaService) {
        this.rotinaService = rotinaService;
    }

    @GetMapping
    @Operation(summary = "Lista as rotinas do cidadão", description = "Devolve só as rotinas de quem está logado, em páginas de até 50 itens.")
    public PaginaResponse<RotinaResponse> listar(@PageableDefault(size = 20) Pageable pageable) {
        return rotinaService.listar(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Mostra uma rotina", description = "Só encontra a rotina se ela for do cidadão autenticado.")
    public RotinaResponse buscar(@PathVariable Long id) {
        return rotinaService.buscar(id);
    }

    @PostMapping
    @Operation(summary = "Cadastra uma rotina", description = "Exige o cabeçalho Idempotency-Key. A mesma chave devolve a rotina já criada, sem duplicar. Ida e volta são duas rotinas.")
    public ResponseEntity<RotinaResponse> criar(
            @RequestHeader("Idempotency-Key") String chave,
            @Valid @RequestBody RotinaRequest request) {
        RotinaService.Criacao criacao = rotinaService.criar(request, chave);
        if (criacao.isNova()) {
            return ResponseEntity.status(201).body(criacao.getRotina());
        }
        return ResponseEntity.ok(criacao.getRotina());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Edita ou desliga uma rotina", description = "Para desligar, envie ativa como false. A rotina continua gravada.")
    public RotinaResponse atualizar(@PathVariable Long id, @Valid @RequestBody RotinaRequest request) {
        return rotinaService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Exclui uma rotina", description = "Não exclui se a rotina já tiver um aviso ligado a ela.")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        rotinaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
