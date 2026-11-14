package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.model.StatusAviso;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class AcompanhamentoDecisor {

    public Optional<Decisao> decidir(Entrada entrada) {
        if (entrada.status() == StatusAviso.ENCERRADO || entrada.status() == StatusAviso.SEM_PREVISAO) {
            return Optional.empty();
        }

        List<VeiculoFeed> futuros = entrada.veiculos().stream()
                .filter(veiculo -> veiculo.chegada().isAfter(entrada.relogio()))
                .toList();

        if (entrada.veiculoPreso() != null) {
            Optional<VeiculoFeed> mesmo = futuros.stream()
                    .filter(veiculo -> veiculo.id().equals(entrada.veiculoPreso()))
                    .findFirst();
            if (mesmo.isEmpty()) {
                if (entrada.status() == null) {
                    return Optional.of(semPrevisao());
                }
                return Optional.of(fechar(entrada.veiculoPreso()));
            }
            VeiculoFeed veiculo = mesmo.get();
            int minutos = minutos(entrada.relogio(), veiculo.chegada());
            if (entrada.status() == null) {
                if (minutos <= 15) {
                    return Optional.of(abrir(veiculo, minutos));
                }
                return Optional.of(soMemoria(veiculo.id()));
            }
            if (entrada.minutosAnteriores() != null && cruzou(entrada.minutosAnteriores(), minutos)) {
                return Optional.of(atualizar(veiculo, minutos));
            }
            return Optional.of(soMemoria(veiculo.id()));
        }

        if (futuros.isEmpty()) {
            if (entrada.status() == null) {
                return Optional.of(semPrevisao());
            }
            return Optional.empty();
        }

        VeiculoFeed escolhido = maisPerto(futuros, entrada.horario());
        int minutos = minutos(entrada.relogio(), escolhido.chegada());
        if (minutos <= 15) {
            return Optional.of(abrir(escolhido, minutos));
        }
        return Optional.of(soMemoria(escolhido.id()));
    }

    static int minutos(Instant relogio, Instant chegada) {
        long segundos = Duration.between(relogio, chegada).getSeconds();
        if (segundos <= 0) {
            return 0;
        }
        long cheios = segundos / 60;
        return (int) (cheios == 0 ? 1 : cheios);
    }

    private VeiculoFeed maisPerto(List<VeiculoFeed> futuros, Instant horario) {
        return futuros.stream()
                .min(Comparator.comparingLong(veiculo -> Math.abs(Duration.between(horario, veiculo.chegada()).toSeconds())))
                .orElseThrow();
    }

    private boolean cruzou(int anterior, int atual) {
        return desceu(anterior, atual, 10) || desceu(anterior, atual, 5) || desceu(anterior, atual, 2);
    }

    private boolean desceu(int anterior, int atual, int marco) {
        return anterior > marco && atual <= marco;
    }

    private Decisao abrir(VeiculoFeed veiculo, int minutos) {
        return new Decisao(true, StatusAviso.ENVIADO, veiculo.id(), minutos, veiculo.idadeGpsSegundos(), veiculo.id(), false);
    }

    private Decisao atualizar(VeiculoFeed veiculo, int minutos) {
        return new Decisao(true, StatusAviso.ATUALIZADO, veiculo.id(), minutos, veiculo.idadeGpsSegundos(), veiculo.id(), false);
    }

    private Decisao fechar(String veiculoId) {
        return new Decisao(true, StatusAviso.ENCERRADO, veiculoId, 0, null, null, true);
    }

    private Decisao semPrevisao() {
        return new Decisao(true, StatusAviso.SEM_PREVISAO, null, null, null, null, true);
    }

    private Decisao soMemoria(String veiculoId) {
        return new Decisao(false, null, veiculoId, null, null, veiculoId, false);
    }

    public record Entrada(
            Instant horario,
            Instant relogio,
            List<VeiculoFeed> veiculos,
            String veiculoPreso,
            Integer minutosAnteriores,
            StatusAviso status
    ) {
    }

    public record Decisao(
            boolean persistir,
            StatusAviso status,
            String veiculoId,
            Integer minutos,
            Integer idadeGpsSegundos,
            String memorarVeiculo,
            boolean limparMemoria
    ) {
    }
}
