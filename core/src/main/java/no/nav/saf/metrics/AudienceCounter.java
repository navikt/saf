package no.nav.saf.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AudienceCounter {
    private final MeterRegistry meterRegistry;

    @Autowired
    public AudienceCounter(MeterRegistry meterRegistry){
        this.meterRegistry = meterRegistry;
    }
    public void increment(String issuer, String audience){
        Counter.builder("dok_saf_audience")
                .tags("issuer", issuer)
                .tags("audience", audience)
                .register(meterRegistry)
                .increment();
    }
}
