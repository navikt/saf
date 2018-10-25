package no.nav.saf.tilgangskontroll.abac.exception;

public class UnhandledObligationException extends RuntimeException {
    public UnhandledObligationException(String obligationId) {
        super("No strategy found to handle obligation with id: " + obligationId);
    }
}