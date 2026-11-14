package br.com.fiap.nextbus.service;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class PosicaoOlhoVivo implements PosicaoFonte {

    private final OlhoVivoConsulta olhoVivo;
    private final PosicaoDemonstracao demonstracao;

    public PosicaoOlhoVivo(OlhoVivoConsulta olhoVivo, PosicaoDemonstracao demonstracao) {
        this.olhoVivo = olhoVivo;
        this.demonstracao = demonstracao;
    }

    @Override
    public List<PosicaoDemonstracao.PosicaoVeiculo> daLinha(CatalogoTransporte.LinhaCatalogo linha) {
        return olhoVivo.posicoes(linha.codigo())
                .map(veiculos -> veiculos.stream()
                        .map(veiculo -> new PosicaoDemonstracao.PosicaoVeiculo(
                                veiculo.id(),
                                linha.letreiro(),
                                linha.sentido(),
                                veiculo.lat(),
                                veiculo.lng(),
                                veiculo.idadeGpsSegundos(),
                                veiculo.acessivel()))
                        .toList())
                .orElseGet(() -> demonstracao.daLinha(linha));
    }
}
