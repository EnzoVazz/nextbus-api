package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.config.RabbitConfig;
import br.com.fiap.nextbus.messaging.AvisoPronto;
import br.com.fiap.nextbus.model.Aviso;
import br.com.fiap.nextbus.model.StatusAviso;
import br.com.fiap.nextbus.repository.AvisoRepository;
import br.com.fiap.nextbus.repository.RotinaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
public class AvisoGravador {

    private static final Logger log = LoggerFactory.getLogger(AvisoGravador.class);

    private final AvisoRepository avisoRepository;
    private final RotinaRepository rotinaRepository;

    public AvisoGravador(AvisoRepository avisoRepository, RotinaRepository rotinaRepository) {
        this.avisoRepository = avisoRepository;
        this.rotinaRepository = rotinaRepository;
    }

    @RabbitListener(queues = RabbitConfig.FILA_AVISO)
    @Transactional
    public void gravar(AvisoPronto mensagem) {
        LocalDate data = LocalDate.parse(mensagem.data());
        Aviso aviso = avisoRepository.findByRotinaIdAndDataAviso(mensagem.rotinaId(), data)
                .orElseGet(() -> novo(mensagem, data));
        if (aviso.getId() != null
                && (aviso.getStatus() == StatusAviso.ENCERRADO || aviso.getStatus() == StatusAviso.SEM_PREVISAO)) {
            return;
        }
        aviso.setLetreiro(cortar(mensagem.letreiro(), 20));
        aviso.setNomePonto(cortar(mensagem.nomePonto(), 160));
        aviso.setIdentificadorVeiculo(cortar(mensagem.identificadorVeiculo(), 20));
        aviso.setMinutos(mensagem.minutos());
        aviso.setIdadeGpsSegundos(mensagem.idadeGpsSegundos());
        aviso.setTexto(cortar(mensagem.texto(), 400));
        aviso.setStatus(mensagem.status());
        avisoRepository.save(aviso);
        log.info("Push do aviso {}: {}", aviso.getStatus(),
                TextoAvisoService.textoCurto(aviso.getLetreiro(), aviso.getNomePonto(), aviso.getMinutos()));
    }

    private Aviso novo(AvisoPronto mensagem, LocalDate data) {
        Aviso aviso = new Aviso();
        aviso.setRotina(rotinaRepository.getReferenceById(mensagem.rotinaId()));
        aviso.setDataAviso(data);
        return aviso;
    }

    private String cortar(String valor, int limite) {
        if (valor == null || valor.length() <= limite) {
            return valor;
        }
        return valor.substring(0, limite);
    }
}
