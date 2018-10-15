package no.nav.saf;

import io.micrometer.core.instrument.MeterRegistry;
import no.nav.saf.fasit.ServiceuserAlias;
import no.nav.saf.metrics.DokTimedAspect;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@ComponentScan
@Configuration
@EnableConfigurationProperties(ServiceuserAlias.class)
public class ApplicationConfig {

    @Bean
    public DokTimedAspect timedAspect(MeterRegistry meterRegistry) {
        return new DokTimedAspect(meterRegistry);
    }
}
