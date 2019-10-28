package no.nav.saf.tilgangskontroll.validation;

import org.jose4j.http.SimpleGet;

public interface SimpleGetResolver {
    SimpleGet resolve(String issuerUrl, String proxyAddress);
}