package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.model.Sentido;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Component
public class CatalogoTransporte {

    private final List<ParadaCatalogo> paradas;
    private final List<LinhaCatalogo> linhas;

    public CatalogoTransporte() {
        try {
            JsonMapper mapper = JsonMapper.builder().build();
            this.paradas = ler(mapper, "/catalogo/paradas.json", new TypeReference<List<ParadaCatalogo>>() {
            });
            this.linhas = ler(mapper, "/catalogo/linhas.json", new TypeReference<List<LinhaCatalogo>>() {
            });
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Não foi possível ler o catálogo de linhas e paradas.", ex);
        }
    }

    public List<LinhaCatalogo> linhas() {
        return linhas;
    }

    public List<LinhaCatalogo> buscarLinhas(String termo) {
        String alvo = normalizar(termo);
        if (alvo.isBlank()) {
            return linhas;
        }
        return linhas.stream()
                .filter(linha -> normalizar(linha.codigo()).contains(alvo)
                        || normalizar(linha.letreiro()).contains(alvo)
                        || normalizar(linha.nome()).contains(alvo))
                .toList();
    }

    public Optional<LinhaCatalogo> linha(String codigo) {
        return linhas.stream().filter(linha -> linha.codigo().equals(codigo)).findFirst();
    }

    public Optional<ParadaCatalogo> parada(String codigo) {
        return paradas.stream().filter(parada -> parada.codigo().equals(codigo)).findFirst();
    }

    public Optional<ParadaCatalogo> porEndereco(String texto) {
        String alvo = normalizar(texto);
        Optional<ParadaCatalogo> exata = paradas.stream()
                .filter(parada -> normalizar(parada.nome()).equals(alvo))
                .findFirst();
        if (exata.isPresent()) {
            return exata;
        }
        return paradas.stream()
                .filter(parada -> normalizar(parada.nome()).contains(alvo))
                .findFirst();
    }

    public List<ParadaProxima> proximas(double lat, double lng, int raioMetros) {
        return paradas.stream()
                .map(parada -> new ParadaProxima(parada, Distancia.metros(lat, lng, parada.lat(), parada.lng())))
                .filter(proxima -> proxima.distanciaMetros() <= raioMetros)
                .sorted(Comparator.comparingInt(ParadaProxima::distanciaMetros))
                .toList();
    }

    private <T> List<T> ler(JsonMapper mapper, String caminho, TypeReference<List<T>> tipo) {
        try (InputStream entrada = getClass().getResourceAsStream(caminho)) {
            if (entrada == null) {
                return List.of();
            }
            return mapper.readValue(entrada, tipo);
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível ler " + caminho + ".", ex);
        }
    }

    static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }

    public record ParadaCatalogo(String codigo, String nome, double lat, double lng) {
    }

    public record LinhaCatalogo(String codigo, String letreiro, Sentido sentido, String nome, List<String> paradas) {
    }

    public record ParadaProxima(ParadaCatalogo parada, int distanciaMetros) {
    }
}
