package br.com.fiap.nextbus.service;

import br.com.fiap.nextbus.config.RabbitConfig;
import br.com.fiap.nextbus.messaging.AvisoPronto;
import br.com.fiap.nextbus.messaging.RotinaNaJanela;
import br.com.fiap.nextbus.model.Aviso;
import br.com.fiap.nextbus.model.Rotina;
import br.com.fiap.nextbus.repository.AvisoRepository;
import br.com.fiap.nextbus.repository.RotinaRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

@Component
public class RotinaJanelaConsumer {

    private final RotinaRepository rotinaRepository;
    private final AvisoRepository avisoRepository;
    private final PrevisaoFonte previsaoFonte;
    private final AcompanhamentoDecisor decisor = new AcompanhamentoDecisor();
    private final AcompanhamentoMemoria memoria;
    private final ComunicadoCatalogo comunicados;
    private final TextoAvisoService textoAvisoService;
    private final RabbitTemplate rabbitTemplate;
    private final Clock clock;

    public RotinaJanelaConsumer(RotinaRepository rotinaRepository,
                                AvisoRepository avisoRepository,
                                PrevisaoFonte previsaoFonte,
                                AcompanhamentoMemoria memoria,
                                ComunicadoCatalogo comunicados,
                                TextoAvisoService textoAvisoService,
                                RabbitTemplate rabbitTemplate,
                                Clock clock) {
        this.rotinaRepository = rotinaRepository;
        this.avisoRepository = avisoRepository;
        this.previsaoFonte = previsaoFonte;
        this.memoria = memoria;
        this.comunicados = comunicados;
        this.textoAvisoService = textoAvisoService;
        this.rabbitTemplate = rabbitTemplate;
        this.clock = clock;
    }

    @RabbitListener(queues = RabbitConfig.FILA_ROTINA)
    public void consumir(RotinaNaJanela mensagem) {
        LocalDate data = LocalDate.parse(mensagem.data());
        Rotina rotina = rotinaRepository.findById(mensagem.rotinaId()).orElse(null);
        if (rotina == null || !Boolean.TRUE.equals(rotina.getAtiva())) {
            return;
        }

        Aviso aviso = avisoRepository.findByRotinaIdAndDataAviso(rotina.getId(), data).orElse(null);
        LocalTime horario = LocalTime.parse(rotina.getHorarioEmbarque());
        Instant horarioInstant = data.atTime(horario).atZone(PrevisaoZona.ZONA).toInstant();
        PrevisaoFeed feed = previsaoFonte.consultar(rotina.getCodigoParada(), rotina.getCodigoLinha(), data, horario, clock);
        String preso = veiculoPreso(rotina.getId(), data, aviso);

        Optional<AcompanhamentoDecisor.Decisao> decisao = decisor.decidir(new AcompanhamentoDecisor.Entrada(
                horarioInstant,
                feed.relogio(),
                feed.veiculos(),
                preso,
                aviso == null ? null : aviso.getMinutos(),
                aviso == null ? null : aviso.getStatus()
        ));
        if (decisao.isEmpty()) {
            return;
        }

        AcompanhamentoDecisor.Decisao escolha = decisao.get();
        if (escolha.limparMemoria()) {
            memoria.limpar(rotina.getId(), data);
        } else if (escolha.memorarVeiculo() != null) {
            memoria.gravar(rotina.getId(), data, escolha.memorarVeiculo());
        }
        if (!escolha.persistir()) {
            return;
        }

        String comunicado = comunicados.vigente(rotina.getCodigoLinha(), feed.relogio()).orElse(null);
        String texto = textoAvisoService.gerar(new DadosAviso(
                rotina.getLetreiro(),
                rotina.getNomeParada(),
                escolha.minutos(),
                escolha.idadeGpsSegundos(),
                comunicado
        ));
        rabbitTemplate.convertAndSend(RabbitConfig.FILA_AVISO, new AvisoPronto(
                rotina.getId(),
                data.toString(),
                rotina.getLetreiro(),
                rotina.getNomeParada(),
                escolha.veiculoId(),
                escolha.minutos(),
                escolha.idadeGpsSegundos(),
                texto,
                escolha.status()
        ));
    }

    private String veiculoPreso(Long rotinaId, LocalDate data, Aviso aviso) {
        if (aviso != null && aviso.getIdentificadorVeiculo() != null) {
            return aviso.getIdentificadorVeiculo();
        }
        return memoria.buscar(rotinaId, data);
    }
}
