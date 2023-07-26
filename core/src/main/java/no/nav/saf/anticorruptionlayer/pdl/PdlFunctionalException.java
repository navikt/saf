package no.nav.saf.anticorruptionlayer.pdl;


import no.nav.saf.exceptions.SafFunctionalException;

public class PdlFunctionalException extends SafFunctionalException {
    public PdlFunctionalException(String message) {
        super(message);
    }

    public PdlFunctionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
