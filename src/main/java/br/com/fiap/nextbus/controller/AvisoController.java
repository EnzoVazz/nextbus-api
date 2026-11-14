package br.com.fiap.nextbus.controller;

import br.com.fiap.nextbus.dto.AvisoResponse;
import br.com.fiap.nextbus.service.AvisoConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/avisos")
@Tag(name = "3. Avisos", description = "Avisos do dia do cidadão autenticado")
public class AvisoController {

    private final AvisoConsultaService avisoConsultaService;

    public AvisoController(AvisoConsultaService avisoConsultaService) {
        this.avisoConsultaService = avisoConsultaService;
    }

    @GetMapping
    @Operation(summary = "Lista os avisos de hoje", description = "Devolve os avisos de quem está logado. minutos é o número da tela, textoCurto é a notificação, texto é a frase da tela e comunicado é a faixa oficial.")
    public List<AvisoResponse> listar() {
        return avisoConsultaService.doDia();
    }
}
