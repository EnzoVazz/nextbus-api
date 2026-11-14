package br.com.fiap.nextbus.controller;

import br.com.fiap.nextbus.service.SaudeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "9. Saúde", description = "Banco e situação da Olho Vivo")
public class HealthController {

    private final SaudeService saudeService;

    public HealthController(SaudeService saudeService) {
        this.saudeService = saudeService;
    }

    @GetMapping("/health")
    @SecurityRequirements
    @Operation(summary = "Verifica o banco e a Olho Vivo", description = "Público. olhoVivo fica ok quando o login responde, demonstracao sem token e indisponivel quando o login falha.")
    public ResponseEntity<SaudeService.Saude> health() {
        SaudeService.Saude saude = saudeService.verificar();
        if (saude.ok()) {
            return ResponseEntity.ok(saude);
        }
        return ResponseEntity.status(503).body(saude);
    }
}
