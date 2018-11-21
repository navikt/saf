package no.nav.saf.exceptionhandler;

import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.exceptions.SafFunctionalException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Slf4j
@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionHandler {

	@Inject
	private MeterRegistry meterRegistry;

	@Override
	public void accept(DataFetcherExceptionHandlerParameters handlerParameters) {
		Throwable exception = handlerParameters.getException();
		SourceLocation sourceLocation = handlerParameters.getField().getSourceLocation();
		ExecutionPath path = handlerParameters.getPath();

		CustomExceptionWhileDataFetching error = new CustomExceptionWhileDataFetching(path, exception, sourceLocation);
		handlerParameters.getExecutionContext().addError(error);
		log.warn(error.getMessage(), exception);

		incrementExceptionCounter("dok_request_seconds_count", error.getException(), meterRegistry, "process", "dokumentOversikt", "path");
	}

	private String getPathAsString(List<Object> pathList) {
		return StringUtils.chop(pathList.stream().map(p -> p + "/").collect(Collectors.joining()));
	}

	public static void incrementExceptionCounter(String counterName, Throwable throwable, MeterRegistry meterRegistry, String... otherParameters) {
		Counter.builder(counterName)
				.tags("error_type", isFunctionalException(throwable) ? "functional" : "technical")
				.tags("exception_name", throwable.getClass().getSimpleName())
				.tags(otherParameters)
				.register(meterRegistry)
				.increment();
	}

	public static boolean isFunctionalException(Throwable e) {
		return e instanceof SafFunctionalException;
	}

}
