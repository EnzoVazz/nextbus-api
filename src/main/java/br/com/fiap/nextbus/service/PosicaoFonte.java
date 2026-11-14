package br.com.fiap.nextbus.service;

import java.util.List;

public interface PosicaoFonte {

    List<PosicaoDemonstracao.PosicaoVeiculo> daLinha(CatalogoTransporte.LinhaCatalogo linha);
}
