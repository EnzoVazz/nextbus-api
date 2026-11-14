package br.com.fiap.nextbus.controller;

import br.com.fiap.nextbus.service.CatalogoTransporte;
import br.com.fiap.nextbus.service.MapaConsultaService;
import br.com.fiap.nextbus.service.PosicaoDemonstracao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/linhas")
@Tag(name = "4. Linhas", description = "Busca de linhas e posição dos ônibus")
public class LinhaController {

    private final MapaConsultaService mapaConsultaService;

    public LinhaController(MapaConsultaService mapaConsultaService) {
        this.mapaConsultaService = mapaConsultaService;
    }

    @GetMapping
    @Operation(summary = "Busca linhas por número ou nome", description = "Não exige rotina cadastrada. A consulta q pode ficar vazia.")
    public List<CatalogoTransporte.LinhaCatalogo> buscar(@RequestParam(name = "q", required = false, defaultValue = "") String termo) {
        return mapaConsultaService.linhas(termo);
    }

    @GetMapping("/{codigo}/posicoes")
    @Operation(summary = "Posição dos ônibus da linha", description = "Consulta a Olho Vivo. Se ela não responder, usa a demonstração. Cada veículo traz letreiro, sentido, acessibilidade e há quantos segundos o GPS foi capturado.")
    public List<PosicaoDemonstracao.PosicaoVeiculo> posicoes(@PathVariable String codigo) {
        return mapaConsultaService.posicoes(codigo);
    }
}
