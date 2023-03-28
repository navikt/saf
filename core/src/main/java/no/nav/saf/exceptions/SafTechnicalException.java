package no.nav.saf.exceptions;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.ArrayList;
import java.util.List;


@Getter
public class SafTechnicalException extends RuntimeException implements GraphQLError {
	private final HttpStatusCode httpStatusCode;

	public SafTechnicalException(HttpStatus httpStatus) {
		super();
		this.httpStatusCode = httpStatus;
	}

	public SafTechnicalException(String message, HttpStatusCode httpStatus) {
		super(message);
		this.httpStatusCode = httpStatus;
	}

	public SafTechnicalException(String message, Throwable cause, HttpStatusCode httpStatus) {
		super(message, cause);
		this.httpStatusCode = httpStatus;
	}

	public SafTechnicalException(String message, Throwable cause) {
		super(message, cause);
		this.httpStatusCode = null;
	}

	@Override
	public List<SourceLocation> getLocations() {
		return new ArrayList<>();
	}

	@Override
	public ErrorType getErrorType() {
		return ErrorType.DataFetchingException;
	}

}
