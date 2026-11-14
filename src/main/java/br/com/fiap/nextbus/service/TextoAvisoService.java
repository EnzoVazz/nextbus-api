package br.com.fiap.nextbus.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TextoAvisoService {

    private final GeradorTexto gerador;

    @Autowired
    public TextoAvisoService(ObjectProvider<ChatClient.Builder> builders,
                             @Value("${spring.ai.openai.api-key:}") String apiKey) {
        if (apiKey == null || apiKey.isBlank() || "nao-configurada".equals(apiKey)) {
            this.gerador = null;
        } else {
            ChatClient.Builder builder = builders.getIfAvailable();
            if (builder == null) {
                this.gerador = null;
            } else {
                ChatClient client = builder.build();
                this.gerador = instrucao -> client.prompt().user(instrucao).call().content();
            }
        }
    }

    TextoAvisoService(GeradorTexto gerador) {
        this.gerador = gerador;
    }

    public String gerar(DadosAviso dados) {
        String padrao = frasePadrao(dados);
        if (gerador == null) {
            return limitar(padrao);
        }
        try {
            String resposta = gerador.gerar(instrucao(padrao));
            if (resposta == null || resposta.isBlank() || pareceFicha(resposta)) {
                return limitar(padrao);
            }
            return limitar(resposta.trim());
        } catch (RuntimeException ex) {
            return limitar(padrao);
        }
    }

    public static String textoCurto(String letreiro, String nomePonto, Integer minutos) {
        if (minutos == null) {
            return letreiro + " sem previsão no seu ponto agora.";
        }
        return letreiro + " no ponto " + nomePonto + ": " + minutos + " minutos";
    }

    static String frasePadrao(DadosAviso dados) {
        String frase;
        if (dados.minutos() == null) {
            frase = dados.letreiro() + " sem previsão no seu ponto agora.";
        } else {
            frase = dados.letreiro() + " no ponto " + dados.nomePonto() + ": " + dados.minutos() + " minutos";
            if (dados.idadeGpsSegundos() != null) {
                frase += ". GPS capturado há " + dados.idadeGpsSegundos() + " segundos";
            }
        }
        if (dados.comunicado() != null && !dados.comunicado().isBlank()) {
            frase += " Comunicado: " + dados.comunicado();
        }
        return frase;
    }

    private String instrucao(String aviso) {
        return """
                Reescreva o aviso em uma ou duas frases naturais, em português, para a tela do celular.
                Mantenha letreiro, ponto, minutos e comunicado. Não invente acidente, desvio, lotação ou atraso.
                Não use rótulos, travessão nem diga que um dado não foi informado.
                Aviso: %s
                """.formatted(aviso);
    }

    private boolean pareceFicha(String resposta) {
        String baixa = resposta.toLowerCase();
        return baixa.contains("não informado")
                || baixa.contains("nao informado")
                || baixa.contains("idade do gps")
                || resposta.contains("—");
    }

    private String limitar(String texto) {
        if (texto.length() <= 400) {
            return texto;
        }
        return texto.substring(0, 400);
    }

    interface GeradorTexto {
        String gerar(String instrucao);
    }
}
