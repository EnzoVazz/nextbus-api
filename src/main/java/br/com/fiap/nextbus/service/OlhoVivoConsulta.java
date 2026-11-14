package br.com.fiap.nextbus.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OlhoVivoConsulta {

    Optional<PrevisaoFeed> previsao(String codigoParada, String codigoLinha, LocalDate data);

    Optional<List<OlhoVivoJson.PosicaoBruta>> posicoes(String codigoLinha);

    String situacao();
}
