package no.nav.saf.anticorruptionlayer.azure;


/**
 * @author Joakim Bjørnstad, Jbit AS
 */
public class AzureTokenException extends RuntimeException {
    public AzureTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
