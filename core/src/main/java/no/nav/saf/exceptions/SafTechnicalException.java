package no.nav.saf.exceptions;

import lombok.Getter;
import no.nav.saf.graphql.ErrorCode;
import org.springframework.http.HttpStatusCode;

import java.util.Map;

import static no.nav.saf.exceptions.SafFunctionalException.resolveToCode;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Getter
public class SafTechnicalException extends RuntimeException {
	private final ErrorCode errorCode;

	public SafTechnicalException(String message, HttpStatusCode httpStatus) {
		this(message, null, httpStatus);
	}

	public SafTechnicalException(String message, Throwable cause, HttpStatusCode httpStatus) {
		super(message, cause);
		this.errorCode = resolveToCode(httpStatus);
	}

	public SafTechnicalException(String message, Throwable cause) {
		this(message, cause, INTERNAL_SERVER_ERROR);
	}

	public SafTechnicalException(String message) {
		this(message, (Throwable) null);
	}

	Map<String,Object> getExtensions() {
		return Map.of("code", errorCode.getText());
	}

	public AnonymizedSafTechnicalExceptionWrapper asAnonymizedGraphQlError() {
		return new AnonymizedSafTechnicalExceptionWrapper(this);
	}

	public MessageSafTechnicalExceptionWrapper asMessageGraphQlError() {
		return new MessageSafTechnicalExceptionWrapper(this);
	}
}
