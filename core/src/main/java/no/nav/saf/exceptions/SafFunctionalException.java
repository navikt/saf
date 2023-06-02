package no.nav.saf.exceptions;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

import java.util.ArrayList;
import java.util.List;

import static graphql.ErrorType.DataFetchingException;


@Getter
public class SafFunctionalException extends RuntimeException implements GraphQLError {
	private final HttpStatusCode httpStatusCode;

	public SafFunctionalException() {
		super();
		httpStatusCode = null;
	}

	public SafFunctionalException(String message) {
		super(message);
		this.httpStatusCode = null;
	}

	public SafFunctionalException(String message, HttpStatusCode httpStatusCode) {
		super(message);
		this.httpStatusCode = httpStatusCode;
	}

	public SafFunctionalException(String message, Throwable cause, HttpStatusCode httpStatusCode) {
		super(message, cause);
		this.httpStatusCode = httpStatusCode;
	}

	public SafFunctionalException(String message, Throwable cause) {
		super(message, cause);
		this.httpStatusCode = null;
	}

	@Override
	public List<SourceLocation> getLocations() {
		return new ArrayList<>();
	}

	@Override
	public ErrorType getErrorType() {
		return DataFetchingException;
	}
}
