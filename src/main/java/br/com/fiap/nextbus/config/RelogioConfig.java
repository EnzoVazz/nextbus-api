package br.com.fiap.nextbus.config;

import br.com.fiap.nextbus.service.PrevisaoZona;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class RelogioConfig {

    @Bean
    public Clock clock() {
        return Clock.system(PrevisaoZona.ZONA);
    }
}
