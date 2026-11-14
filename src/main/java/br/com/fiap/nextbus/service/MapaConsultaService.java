package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.exception.NaoEncontradoException;
import br.com.fiap.nextbus.exception.RequisicaoInvalidaException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class MapaConsultaService {

    private static final int RAIO_METROS = 800;
    private static final int GPS_VELHO = 180;

    private final CatalogoTransporte catalogo;
    private final PosicaoFonte posicoes;
    private final PrevisaoFonte previsaoFonte;
    private final Clock clock;
    private final ConsultaCache<List<PosicaoDemonstracao.PosicaoVeiculo>> cachePosicao = new ConsultaCache<>();
    private final ConsultaCache<PrevisaoFeed> cachePrevisao = new ConsultaCache<>();

    public MapaConsultaService(CatalogoTransporte catalogo,
                               PosicaoFonte posicoes,
                               PrevisaoFonte previsaoFonte,
                               Clock clock) {
        this.catalogo = catalogo;
        this.posicoes = posicoes;
        this.previsaoFonte = previsaoFonte;
        this.clock = clock;
    }

    public List<CatalogoTransporte.LinhaCatalogo> linhas(String termo) {
        return catalogo.buscarLinhas(termo);
    }

    public List<CatalogoTransporte.ParadaProxima> paradasProximas(double lat, double lng) {
        validarCoordenada(lat, lng);
        return catalogo.proximas(lat, lng, RAIO_METROS);
    }

    public List<PosicaoDemonstracao.PosicaoVeiculo> posicoes(String codigoLinha) {
        CatalogoTransporte.LinhaCatalogo linha = catalogo.linha(codigoLinha)
                .orElseThrow(() -> new NaoEncontradoException("Linha não encontrada."));
        return cachePosicao.obter("posicao:" + codigoLinha, () -> posicoes.daLinha(linha));
    }

    public PrevisaoResposta previsao(String codigoParada, String codigoLinha) {
        catalogo.parada(codigoParada).orElseThrow(() -> new NaoEncontradoException("Parada não encontrada."));
        catalogo.linha(codigoLinha).orElseThrow(() -> new NaoEncontradoException("Linha não encontrada."));
        PrevisaoFeed feed = cachePrevisao.obter(
                "previsao:" + codigoParada + ":" + codigoLinha,
                () -> consultar(codigoParada, codigoLinha));
        List<VeiculoPrevisto> veiculos = feed.veiculos().stream()
                .filter(veiculo -> veiculo.chegada().isAfter(feed.relogio()))
                .map(veiculo -> new VeiculoPrevisto(
                        veiculo.id(),
                        AcompanhamentoDecisor.minutos(feed.relogio(), veiculo.chegada()),
                        veiculo.idadeGpsSegundos()))
                .sorted(Comparator.comparingInt(VeiculoPrevisto::minutos))
                .toList();
        return new PrevisaoResposta(feed.relogio().toString(), veiculos);
    }

    public List<LinhaSemGps> linhasSemGps() {
        List<LinhaSemGps> semGps = new ArrayList<>();
        for (CatalogoTransporte.LinhaCatalogo linha : catalogo.linhas()) {
            int menorIdade = posicoes(linha.codigo()).stream()
                    .mapToInt(PosicaoDemonstracao.PosicaoVeiculo::idadeGpsSegundos)
                    .min()
                    .orElse(GPS_VELHO + 1);
            if (menorIdade > GPS_VELHO) {
                semGps.add(new LinhaSemGps(linha.codigo(), linha.letreiro(), menorIdade));
            }
        }
        return semGps;
    }

    public List<OpcaoViagem> planejar(double origemLat, double origemLng, String destino) {
        validarCoordenada(origemLat, origemLng);
        CatalogoTransporte.ParadaCatalogo destinoParada = catalogo.porEndereco(destino)
                .orElseThrow(() -> new RequisicaoInvalidaException("Não encontrei esse endereço no catálogo."));
        List<CatalogoTransporte.ParadaProxima> pertoOrigem = catalogo.proximas(origemLat, origemLng, RAIO_METROS);
        List<CatalogoTransporte.ParadaProxima> pertoDestino = catalogo.proximas(
                destinoParada.lat(), destinoParada.lng(), RAIO_METROS);

        List<OpcaoViagem> opcoes = new ArrayList<>();
        for (CatalogoTransporte.LinhaCatalogo linha : catalogo.linhas()) {
            OpcaoViagem opcao = melhorTrecho(linha, pertoOrigem, pertoDestino, origemLat, origemLng);
            if (opcao != null) {
                opcoes.add(opcao);
            }
        }
        return opcoes.stream()
                .sorted(Comparator.comparingInt(OpcaoViagem::minutos).thenComparingInt(OpcaoViagem::caminhadaMetros))
                .limit(3)
                .toList();
    }

    private OpcaoViagem melhorTrecho(CatalogoTransporte.LinhaCatalogo linha,
                                    List<CatalogoTransporte.ParadaProxima> pertoOrigem,
                                    List<CatalogoTransporte.ParadaProxima> pertoDestino,
                                    double origemLat,
                                    double origemLng) {
        OpcaoViagem melhor = null;
        for (CatalogoTransporte.ParadaProxima origem : pertoOrigem) {
            int embarque = linha.paradas().indexOf(origem.parada().codigo());
            if (embarque < 0) {
                continue;
            }
            for (CatalogoTransporte.ParadaProxima destino : pertoDestino) {
                int desembarque = linha.paradas().indexOf(destino.parada().codigo());
                if (desembarque <= embarque) {
                    continue;
                }
                int caminhada = Distancia.metros(origemLat, origemLng, origem.parada().lat(), origem.parada().lng());
                int minutos = minutosNaParada(origem.parada().codigo(), linha.codigo());
                OpcaoViagem candidata = new OpcaoViagem(
                        origem.parada().codigo(),
                        origem.parada().nome(),
                        linha.codigo(),
                        linha.letreiro(),
                        linha.sentido(),
                        minutos,
                        caminhada
                );
                if (melhor == null || caminhada < melhor.caminhadaMetros()) {
                    melhor = candidata;
                }
            }
        }
        return melhor;
    }

    private int minutosNaParada(String codigoParada, String codigoLinha) {
        PrevisaoFeed feed = consultar(codigoParada, codigoLinha);
        return feed.veiculos().stream()
                .filter(veiculo -> veiculo.chegada().isAfter(feed.relogio()))
                .mapToInt(veiculo -> AcompanhamentoDecisor.minutos(feed.relogio(), veiculo.chegada()))
                .min()
                .orElse(0);
    }

    private PrevisaoFeed consultar(String codigoParada, String codigoLinha) {
        return previsaoFonte.consultar(codigoParada, codigoLinha, LocalDate.now(clock), LocalTime.now(clock), clock);
    }

    private void validarCoordenada(double lat, double lng) {
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new RequisicaoInvalidaException("Informe uma coordenada válida.");
        }
    }

    public record VeiculoPrevisto(String id, int minutos, int idadeGpsSegundos) {
    }

    public record PrevisaoResposta(String relogio, List<VeiculoPrevisto> veiculos) {
    }

    public record LinhaSemGps(String codigo, String letreiro, int idadeGpsSegundos) {
    }

    public record OpcaoViagem(
            String codigoParada,
            String nomeParada,
            String codigoLinha,
            String letreiro,
            br.com.fiap.nextbus.model.Sentido sentido,
            int minutos,
            int caminhadaMetros
    ) {
    }
}
