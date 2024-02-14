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
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


@Getter
public class SafFunctionalException extends RuntimeException implements GraphQLError {
	private final HttpStatusCode httpStatusCode;
	private final ErrorClassification errorType;

	public SafFunctionalException(String message) {
		super(message);
		this.httpStatusCode = INTERNAL_SERVER_ERROR;
		this.errorType = DataFetchingException;
	}

	public SafFunctionalException(String message, HttpStatusCode httpStatusCode) {
		super(message);
		this.httpStatusCode = httpStatusCode;
		this.errorType = DataFetchingException;
	}

	public SafFunctionalException(String message, ErrorCode errorCode) {
		super(message);
		this.httpStatusCode = errorCode.getStatusCode();
		this.errorType = errorCode.getType();
	}

	public SafFunctionalException(String message, Throwable cause, HttpStatusCode httpStatusCode) {
		super(message, cause);
		this.httpStatusCode = httpStatusCode;
		this.errorType = DataFetchingException;
	}

	public SafFunctionalException(String message, Throwable cause) {
		super(message, cause);
		this.httpStatusCode = INTERNAL_SERVER_ERROR;
		this.errorType = DataFetchingException;
	}

	@Override
	public List<SourceLocation> getLocations() {
		return new ArrayList<>();
	}

	@Override
	public ErrorClassification getErrorType() {
		return errorType;
	}

	@Override
	public Map<String, Object> getExtensions() {
		return Map.of("code", ((HttpStatus)httpStatusCode).name().toLowerCase() );
	}
}
