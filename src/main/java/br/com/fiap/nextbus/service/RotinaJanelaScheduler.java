package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.config.RabbitConfig;
import br.com.fiap.nextbus.messaging.RotinaNaJanela;
import br.com.fiap.nextbus.model.Aviso;
import br.com.fiap.nextbus.model.Rotina;
import br.com.fiap.nextbus.model.StatusAviso;
import br.com.fiap.nextbus.repository.AvisoRepository;
import br.com.fiap.nextbus.repository.RotinaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Component
public class RotinaJanelaScheduler {

    private static final Logger log = LoggerFactory.getLogger(RotinaJanelaScheduler.class);

    private final RotinaRepository rotinaRepository;
    private final AvisoRepository avisoRepository;
    private final JanelaRotina janelaRotina;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    public RotinaJanelaScheduler(RotinaRepository rotinaRepository,
                                 AvisoRepository avisoRepository,
                                 JanelaRotina janelaRotina,
                                 RabbitTemplate rabbitTemplate,
                                 Clock clock) {
        this.rotinaRepository = rotinaRepository;
        this.avisoRepository = avisoRepository;
        this.janelaRotina = janelaRotina;
        this.rabbitTemplate = rabbitTemplate;
        this.clock = clock;
    }

    @Scheduled(fixedDelay = 30000, initialDelay = 15000)
    public void publicar() {
        LocalDate hoje = LocalDate.now(clock);
        LocalTime agora = LocalTime.now(clock);
        for (Rotina rotina : rotinaRepository.findAtivas()) {
            if (!janelaRotina.estaNaJanela(rotina.getDiasSemana(), rotina.getHorarioEmbarque(), true, hoje, agora)) {
                continue;
            }
            if (jaEncerrou(rotina.getId(), hoje)) {
                continue;
            }
            try {
                rabbitTemplate.convertAndSend(RabbitConfig.FILA_ROTINA, new RotinaNaJanela(rotina.getId(), hoje.toString()));
            } catch (AmqpException ex) {
                log.warn("RabbitMQ indisponível. A rotina {} fica para a próxima consulta.", rotina.getId());
            }
        }
    }

    private boolean jaEncerrou(Long rotinaId, LocalDate hoje) {
        Optional<Aviso> aviso = avisoRepository.findByRotinaIdAndDataAviso(rotinaId, hoje);
        if (aviso.isEmpty()) {
            return false;
        }
        StatusAviso status = aviso.get().getStatus();
        return status == StatusAviso.ENCERRADO || status == StatusAviso.SEM_PREVISAO;
    }
}
