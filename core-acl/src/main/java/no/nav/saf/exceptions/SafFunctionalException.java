package no.nav.saf.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class SafFunctionalException extends RuntimeException {
	private final HttpStatus httpStatus;

	public SafFunctionalException(HttpStatus httpStatus) {
		super();
		this.httpStatus = httpStatus;
	}

	public SafFunctionalException(String message, HttpStatus httpStatus) {
		super(message);
		this.httpStatus = httpStatus;
	}

	public SafFunctionalException(String message, Throwable cause, HttpStatus httpStatus) {
		super(message, cause);
		this.httpStatus = httpStatus;
	}

	public SafFunctionalException(String message, Throwable cause) {
		super(message, cause);
		this.httpStatus = null;
	}
}
