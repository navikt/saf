package no.nav.saf.graphql;

import graphql.ErrorClassification;
import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import java.util.Map;

import static java.util.Collections.singletonMap;

/**
 * Kopiert fra navikt/pdl
 */
@Getter
@RequiredArgsConstructor
// si farvel til denne (?)
public enum ErrorCode {
    FORBIDDEN(ErrorType.ExecutionAborted, "forbidden", HttpStatus.FORBIDDEN),
    NOT_FOUND(ErrorType.ExecutionAborted, "not_found", HttpStatus.NOT_FOUND),
    BAD_REQUEST(ErrorType.ValidationError, "bad_request", HttpStatus.BAD_REQUEST),
    SERVER_ERROR(ErrorType.DataFetchingException, "server_error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final ErrorClassification type;
    private final String text;
	private final HttpStatusCode statusCode;

    public GraphQLError construct(DataFetchingEnvironment env, String message) {
        return GraphqlErrorBuilder.newError(env)
                .message(message)
                .errorType(type)
                .extensions(Map.of("code", text, "reason_code", "", "reason_message", ""))
                .build();
    }
}
