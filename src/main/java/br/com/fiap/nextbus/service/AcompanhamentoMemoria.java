package br.com.fiap.nextbus.service;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AcompanhamentoMemoria {

    private final ConcurrentHashMap<String, String> veiculos = new ConcurrentHashMap<>();

    public String buscar(Long rotinaId, LocalDate data) {
        return veiculos.get(chave(rotinaId, data));
    }

    public void gravar(Long rotinaId, LocalDate data, String veiculoId) {
        veiculos.put(chave(rotinaId, data), veiculoId);
    }

    public void limpar(Long rotinaId, LocalDate data) {
        veiculos.remove(chave(rotinaId, data));
    }

    private String chave(Long rotinaId, LocalDate data) {
        return rotinaId + "|" + data;
    }
}
