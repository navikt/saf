package no.nav.saf.tilgangskontroll.abac.exception;

public class IndeterminateDecisionException extends RuntimeException {
    public IndeterminateDecisionException() {
        super("Got indeterminate result from ABAC");
    }
}