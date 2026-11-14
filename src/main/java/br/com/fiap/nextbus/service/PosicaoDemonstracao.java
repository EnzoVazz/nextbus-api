package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.model.Sentido;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PosicaoDemonstracao implements PosicaoFonte {

    public static final String LINHA_SEM_GPS_RECENTE = "40110";
    public static final int GPS_VELHO_SEGUNDOS = 240;
    public static final int GPS_NOVO_SEGUNDOS = 25;

    private final CatalogoTransporte catalogo;

    public PosicaoDemonstracao(CatalogoTransporte catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public List<PosicaoVeiculo> daLinha(CatalogoTransporte.LinhaCatalogo linha) {
        CatalogoTransporte.ParadaCatalogo parada = catalogo.parada(linha.paradas().get(0)).orElseThrow();
        int idade = LINHA_SEM_GPS_RECENTE.equals(linha.codigo()) ? GPS_VELHO_SEGUNDOS : GPS_NOVO_SEGUNDOS;
        return List.of(new PosicaoVeiculo(
                "p" + linha.codigo(),
                linha.letreiro(),
                linha.sentido(),
                parada.lat(),
                parada.lng(),
                idade,
                true
        ));
    }

    public record PosicaoVeiculo(
            String id,
            String letreiro,
            Sentido sentido,
            double lat,
            double lng,
            int idadeGpsSegundos,
            boolean acessivel
    ) {
    }
}
