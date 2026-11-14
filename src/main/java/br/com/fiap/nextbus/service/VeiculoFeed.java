package br.com.fiap.nextbus.service;

import java.time.Instant;

public record VeiculoFeed(String id, Instant chegada, int idadeGpsSegundos) {
}
