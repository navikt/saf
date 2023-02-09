package no.nav.saf.endpoints.graphql;

import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ResultPath;
import graphql.language.SourceLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {

	@Override
	public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters dataFetcherExceptionHandlerParameters) {
		Throwable exception = dataFetcherExceptionHandlerParameters.getException();
		SourceLocation sourceLocation = dataFetcherExceptionHandlerParameters.getSourceLocation();
		ResultPath path = dataFetcherExceptionHandlerParameters.getPath();

		CustomExceptionWhileDataFetching error = new CustomExceptionWhileDataFetching(path, exception, sourceLocation);
		log.error("Kall til graphql feilet teknisk. path={}, errormsg={}", path, exception.getMessage(), exception);

		return DataFetcherExceptionHandlerResult.newResult()
				.error(error)
				.build();
	}

}
