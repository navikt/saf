package no.nav.saf.context.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class SafTechnicalException extends RuntimeException {

	private final HttpStatus httpStatus;

	public SafTechnicalException(String message, Throwable cause, HttpStatus httpStatus) {
		super(message, cause);
		this.httpStatus = httpStatus;
	}
}
