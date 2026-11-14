package br.com.fiap.nextbus.controller;

import br.com.fiap.nextbus.service.MapaConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/operacao")
@Tag(name = "8. Operação", description = "Painel do gestor")
public class OperacaoController {

    private final MapaConsultaService mapaConsultaService;

    public OperacaoController(MapaConsultaService mapaConsultaService) {
        this.mapaConsultaService = mapaConsultaService;
    }

    @GetMapping("/linhas-sem-gps")
    @Operation(summary = "Linhas sem GPS recente", description = "Lista a linha quando o GPS mais novo tem mais de três minutos.")
    public List<MapaConsultaService.LinhaSemGps> linhasSemGps() {
        return mapaConsultaService.linhasSemGps();
    }
}
