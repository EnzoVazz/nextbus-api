package br.com.fiap.nextbus.controller;

import br.com.fiap.nextbus.service.MapaConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/previsao")
@Tag(name = "6. Previsão", description = "Minutos até o ônibus chegar na parada")
public class PrevisaoController {

    private final MapaConsultaService mapaConsultaService;

    public PrevisaoController(MapaConsultaService mapaConsultaService) {
        this.mapaConsultaService = mapaConsultaService;
    }

    @GetMapping
    @Operation(summary = "Previsão de uma parada e uma linha", description = "Consulta a Olho Vivo. Se ela não responder, usa a demonstração. O resultado fica em cache por cerca de quinze segundos.")
    public MapaConsultaService.PrevisaoResposta previsao(@RequestParam String parada, @RequestParam String linha) {
        return mapaConsultaService.previsao(parada, linha);
    }
}
