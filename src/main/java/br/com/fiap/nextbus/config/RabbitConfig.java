package br.com.fiap.nextbus.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.DefaultJacksonJavaTypeMapper;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String FILA_ROTINA = "nextbus.rotina.na-janela";
    public static final String FILA_AVISO = "nextbus.aviso.atualizado";

    @Bean
    public Queue filaRotinaNaJanela() {
        return new Queue(FILA_ROTINA, true);
    }

    @Bean
    public Queue filaAvisoAtualizado() {
        return new Queue(FILA_AVISO, true);
    }

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        JacksonJsonMessageConverter converter = new JacksonJsonMessageConverter();
        DefaultJacksonJavaTypeMapper tipos = new DefaultJacksonJavaTypeMapper();
        tipos.setTrustedPackages("br.com.fiap.nextbus");
        converter.setJavaTypeMapper(tipos);
        return converter;
    }
}
