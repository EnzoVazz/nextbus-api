package br.com.fiap.nextbus.controller;

import br.com.fiap.nextbus.dto.PlanejarRequest;
import br.com.fiap.nextbus.service.MapaConsultaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/viagens")
@Tag(name = "7. Planejar", description = "Viagem direta, sem baldeação")
public class ViagemController {

    private final MapaConsultaService mapaConsultaService;

    public ViagemController(MapaConsultaService mapaConsultaService) {
        this.mapaConsultaService = mapaConsultaService;
    }

    @PostMapping("/planejar")
    @Operation(summary = "Recomenda ponto, linha e minutos", description = "Até três opções diretas. A caminhada até o ponto aparece só aqui.")
    public List<MapaConsultaService.OpcaoViagem> planejar(@Valid @RequestBody PlanejarRequest request) {
        return mapaConsultaService.planejar(request.getOrigemLat(), request.getOrigemLng(), request.getDestino());
    }
}
