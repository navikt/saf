package no.nav.saf.exceptions;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.language.SourceLocation;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static graphql.ErrorType.DataFetchingException;
import static no.nav.saf.graphql.ErrorCode.SERVER_ERROR;

@Getter
public class SafTechnicalException extends RuntimeException {
	private final HttpStatusCode httpStatusCode;

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

	List<SourceLocation> getLocations() {
		return new ArrayList<>();
	}

	ErrorType getErrorType() {
		return DataFetchingException;
	}

	Map<String,Object> getExtensions() {
		return Map.of("code", SERVER_ERROR.getText());
	}

	public AnonymizedSafTechincalExceptionWrapper asAnonymizedGraphQlError() {
		return new AnonymizedSafTechincalExceptionWrapper(this);
	}
}
