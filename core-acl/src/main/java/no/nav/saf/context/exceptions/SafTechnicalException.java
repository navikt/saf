package no.nav.saf.context.exceptions;

/**
 * The main technical exception type in Joark.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public class SafTechnicalException extends RuntimeException {
	public SafTechnicalException() {
		super();
	}

	public SafTechnicalException(String message) {
		super(message);
	}

	public SafTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}

	public SafTechnicalException(Throwable cause) {
		super(cause);
	}
}
