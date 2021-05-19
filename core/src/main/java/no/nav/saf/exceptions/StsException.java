package no.nav.saf.exceptions;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class StsException extends RuntimeException {
    public StsException(String message, Throwable cause) {
        super(message, cause);
    }
}
