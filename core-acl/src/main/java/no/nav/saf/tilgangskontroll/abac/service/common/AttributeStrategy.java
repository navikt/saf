package no.nav.saf.tilgangskontroll.abac.service.common;

public interface AttributeStrategy<T> {
    boolean isSupported(String attributeId);

    void perform(T attribute);
}