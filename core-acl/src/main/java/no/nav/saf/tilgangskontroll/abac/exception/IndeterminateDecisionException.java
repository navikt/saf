package no.nav.saf.tilgangskontroll.abac.exception;

/**
 * Denne feilen kastes dersom
 * Decision fra abac = "INDETERMINATE" OG XacmlRequest.isFailOnIndeterminate()
 */

public class IndeterminateDecisionException extends RuntimeException {
    public IndeterminateDecisionException() {
        super("Got indeterminate result from ABAC");
    }
}