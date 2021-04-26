package no.nav.saf.anticorruptionlayer.pdl;


public class PersonIkkeFunnetException extends RuntimeException {
    public PersonIkkeFunnetException(String message) {
        super(message);
    }

    public PersonIkkeFunnetException(String message, Throwable cause) {
        super(message, cause);
    }
}
