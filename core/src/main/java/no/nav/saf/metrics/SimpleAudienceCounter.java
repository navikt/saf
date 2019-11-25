package no.nav.saf.metrics;

import io.micrometer.core.instrument.Metrics;

public class SimpleAudienceCounter {
    private String classname;
    public SimpleAudienceCounter(String classname){
        this.classname = classname;
    }
    public void increment(String issuer, String audience){
        Metrics.counter(classname, "type", "audience-counter", "issuer", issuer, "audience", audience).increment();
    }
}
