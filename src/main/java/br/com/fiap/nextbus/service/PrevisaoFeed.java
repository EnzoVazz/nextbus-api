package br.com.fiap.nextbus.service;

import java.time.Instant;
import java.util.List;

public record PrevisaoFeed(Instant relogio, List<VeiculoFeed> veiculos) {
}
