package no.nav.saf.context.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class SafTechnicalException extends RuntimeException {
	private HttpStatus httpStatus;

	public SafTechnicalException(HttpStatus httpStatus) {
		super();
		this.httpStatus = httpStatus;
	}

	public SafTechnicalException(String message, HttpStatus httpStatus) {
		super(message);
		this.httpStatus = httpStatus;
	}

	public SafTechnicalException(String message, Throwable cause, HttpStatus httpStatus) {
		super(message, cause);
		this.httpStatus = httpStatus;
	}
	public SafTechnicalException(String message, Throwable cause) {
		super(message, cause);
	}

	public SafTechnicalException(Throwable cause) {
		super(cause);
	}

}
