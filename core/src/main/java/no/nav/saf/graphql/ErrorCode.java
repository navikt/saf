package no.nav.saf.graphql;

import graphql.ErrorClassification;
import graphql.ErrorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Kopiert fra navikt/pdl
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	FORBIDDEN(ErrorType.ExecutionAborted, "forbidden"),
	NOT_FOUND(ErrorType.ExecutionAborted, "not_found"),
	BAD_REQUEST(ErrorType.ValidationError, "bad_request"),
	SERVER_ERROR(ErrorType.DataFetchingException, "server_error");

	private final ErrorClassification type;
	private final String text;
}
