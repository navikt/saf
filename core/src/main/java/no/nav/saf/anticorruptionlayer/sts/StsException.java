package no.nav.saf.anticorruptionlayer.sts;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class StsException extends RuntimeException {
    public StsException(String message, Throwable cause) {
        super(message, cause);
    }
}
