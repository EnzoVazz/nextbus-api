package br.com.fiap.nextbus.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;

public interface PrevisaoFonte {

    PrevisaoFeed consultar(String codigoParada, String codigoLinha, LocalDate data, LocalTime horario, Clock clock);
}
