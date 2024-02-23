package no.nav.saf.exceptions;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import lombok.Getter;
import no.nav.saf.graphql.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static graphql.ErrorType.DataFetchingException;
import static no.nav.saf.graphql.ErrorCode.SERVER_ERROR;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


@Getter
public class SafFunctionalException extends RuntimeException implements GraphQLError {
	private final ErrorCode errorCode;

	public SafFunctionalException(String message) {
		super(message);
		this.errorCode = SERVER_ERROR;
	}

	public SafFunctionalException(String message, HttpStatusCode httpStatusCode) {
		super(message);
		this.errorCode = resolveToCode(httpStatusCode);
	}

	public SafFunctionalException(String message, ErrorCode errorCode) {
		super(message);
		this.errorCode = errorCode;
	}

	public SafFunctionalException(String message, Throwable cause, HttpStatusCode httpStatusCode) {
		super(message, cause);
		this.errorCode = resolveToCode(httpStatusCode);
	}

	public SafFunctionalException(String message, Throwable cause) {
		super(message, cause);
		this.errorCode = SERVER_ERROR;
	}

	@Override
	public List<SourceLocation> getLocations() {
		return new ArrayList<>();
	}

	@Override
	public ErrorClassification getErrorType() {
		return errorCode.getType();
	}

	@Override
	public Map<String, Object> getExtensions() {
		return Map.of("code", errorCode.getText());
	}

	public static ErrorCode resolveToCode(HttpStatusCode httpStatusCode) {
		if (httpStatusCode instanceof HttpStatus httpStatus) {
			return switch (httpStatus) {
				case BAD_REQUEST -> ErrorCode.BAD_REQUEST;
				case FORBIDDEN -> ErrorCode.FORBIDDEN;
				case NOT_FOUND -> ErrorCode.NOT_FOUND;
				default -> SERVER_ERROR;
			};
		}
		return SERVER_ERROR;
	}

}
