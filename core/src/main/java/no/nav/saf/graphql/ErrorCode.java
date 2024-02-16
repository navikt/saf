package no.nav.saf.graphql;

import graphql.ErrorClassification;
import graphql.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 * Kopiert fra navikt/pdl
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	FORBIDDEN(ErrorType.ExecutionAborted, "forbidden", HttpStatus.FORBIDDEN),
	NOT_FOUND(ErrorType.ExecutionAborted, "not_found", HttpStatus.NOT_FOUND),
	BAD_REQUEST(ErrorType.ValidationError, "bad_request", HttpStatus.BAD_REQUEST),
	SERVER_ERROR(ErrorType.DataFetchingException, "internal_server_error", HttpStatus.INTERNAL_SERVER_ERROR);

	private final ErrorClassification type;
	private final String text;
	private final HttpStatusCode statusCode;
}
