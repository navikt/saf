package no.nav.saf.exceptionhandler;

import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.SafFunctionalException;
import org.springframework.stereotype.Component;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {

	@Override
	public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters dataFetcherExceptionHandlerParameters) {
		Throwable exception = dataFetcherExceptionHandlerParameters.getException();
		SourceLocation sourceLocation = dataFetcherExceptionHandlerParameters.getSourceLocation();
		ExecutionPath path = dataFetcherExceptionHandlerParameters.getPath();

		CustomExceptionWhileDataFetching error = new CustomExceptionWhileDataFetching(path, exception, sourceLocation);
		log.error("Kall til graphql feilet teknisk. path={}, errormsg={}", path, exception.getMessage(), exception);

		return DataFetcherExceptionHandlerResult.newResult()
				.error(error)
				.build();
	}

	static boolean isFunctionalException(Throwable e) {
		return e instanceof SafFunctionalException;
	}
}
