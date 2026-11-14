package br.com.fiap.nextbus.controller;

import br.com.fiap.nextbus.service.CatalogoTransporte;
import br.com.fiap.nextbus.service.MapaConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/paradas")
@Tag(name = "5. Paradas", description = "Paradas perto de um ponto do mapa")
public class ParadaController {

    private final MapaConsultaService mapaConsultaService;

    public ParadaController(MapaConsultaService mapaConsultaService) {
        this.mapaConsultaService = mapaConsultaService;
    }

    @GetMapping("/proximas")
    @Operation(summary = "Paradas num raio de 800 metros", description = "A distância sai do catálogo local de paradas.")
    public List<CatalogoTransporte.ParadaProxima> proximas(@RequestParam double lat, @RequestParam double lng) {
        return mapaConsultaService.paradasProximas(lat, lng);
    }
}
