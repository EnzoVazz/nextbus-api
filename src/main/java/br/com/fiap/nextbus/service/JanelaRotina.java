package br.com.fiap.nextbus.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class JanelaRotina {

    public boolean estaNaJanela(String diasSemana, String horarioEmbarque, boolean ativa, LocalDate hoje, LocalTime agora) {
        if (!ativa || diasSemana == null || horarioEmbarque == null) {
            return false;
        }
        if (!contemDia(diasSemana, hoje.getDayOfWeek().getValue())) {
            return false;
        }
        LocalTime horario = LocalTime.parse(horarioEmbarque.trim());
        LocalTime inicio = horario.minusMinutes(20);
        LocalTime fim = horario.plusMinutes(45);
        if (fim.isAfter(inicio)) {
            return !agora.isBefore(inicio) && !agora.isAfter(fim);
        }
        return !agora.isBefore(inicio) || !agora.isAfter(fim);
    }

    private boolean contemDia(String diasSemana, int dia) {
        for (String parte : diasSemana.split(",")) {
            if (parte.trim().equals(Integer.toString(dia))) {
                return true;
            }
        }
        return false;
    }
}
