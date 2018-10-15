package no.nav.saf.context.exceptions;

/**
 * The main functional exception type in Joark.
 *
 * @author Joakim Bjørnstad, Jbit AS
 */
public class SafFunctionalException extends RuntimeException {
	public SafFunctionalException() {
		super();
	}

	public SafFunctionalException(String message) {
		super(message);
	}

	public SafFunctionalException(String message, Throwable cause) {
		super(message, cause);
	}

	public SafFunctionalException(Throwable cause) {
		super(cause);
	}
}
