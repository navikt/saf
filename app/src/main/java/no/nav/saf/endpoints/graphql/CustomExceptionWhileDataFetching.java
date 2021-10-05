package no.nav.saf.endpoints.graphql;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorHelper;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import lombok.Getter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static graphql.Assert.assertNotNull;
import static java.lang.String.format;
import static no.nav.saf.endpoints.graphql.GraphQLExceptionHandler.isFunctionalException;

/**
 * Copy of @ExceptionWhileDataFetching
 *
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@ToString
@Getter
public class CustomExceptionWhileDataFetching implements GraphQLError {
    private final String message;
    private final List<Object> path;
    private final Throwable exception;
    private final List<SourceLocation> locations;
    private final ExceptionType exceptionType;
    static final long serialVersionUID = 123456789;

    public CustomExceptionWhileDataFetching(ResultPath path, Throwable exception, SourceLocation sourceLocation) {
        this.path = assertNotNull(path).toList();
        this.exception = assertNotNull(exception);
        this.locations = Collections.singletonList(sourceLocation);
        this.message = mkMessage(path, exception);
        this.exceptionType = getExceptionTypeFromThrowable(exception);
    }

    public ExceptionType getExceptionType() {
        return this.exceptionType;
    }

    public ExceptionType getExceptionTypeFromThrowable(Throwable exception) {
        if (isFunctionalException(exception)) {
            return ExceptionType.FUNCTIONAL;
        }
        return ExceptionType.TECHNICAL;
    }

    private String mkMessage(ResultPath path, Throwable exception) {
        return format("Feilet ved henting av data (%s) : %s", path, exception.getMessage());
    }

    public Throwable getException() {
        return exception;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ErrorType getErrorType() {
        return ErrorType.DataFetchingException;
    }

    @Override
    public Map<String, Object> toSpecification() {
        Map<String, Object> specification = GraphqlErrorHelper.toSpecification(this);
        specification.put("exceptionType", getExceptionType());
        specification.put("exception", getException().getClass().getSimpleName());
        return specification;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
        return GraphqlErrorHelper.equals(this, o);
    }

    @Override
    public int hashCode() {
        return GraphqlErrorHelper.hashCode(this);
    }
}
