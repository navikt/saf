package no.nav.saf.tilgangskontroll.abac.exception;

public class JsonUnmarshallingException extends RuntimeException {
    public JsonUnmarshallingException(String message, Throwable cause) {
        super(message, cause);
    }
}