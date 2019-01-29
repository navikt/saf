package no.nav.saf.exceptions;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;


@Getter
public class SafTechnicalException extends RuntimeException  implements GraphQLError {
	private final HttpStatus httpStatus;

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
		this.httpStatus = null;
	}

	@Override
	public List<SourceLocation> getLocations() {
		return null;
	}

	@Override
	public ErrorType getErrorType() {
		return ErrorType.DataFetchingException;
	}

}
