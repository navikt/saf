package no.nav.saf.graphql;

import graphql.GraphQLError;
import graphql.execution.DataFetcherResult;
import graphql.schema.DataFetchingEnvironment;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class GraphQLException extends RuntimeException {
	GraphQLError error;
	Object data;

	public <T> DataFetcherResult<T> toDataFetcherResult() {
		return DataFetcherResult.<T>newResult().error(error).data((T) data).build();
	}

	public static GraphQLException of(ErrorCode code, DataFetchingEnvironment env, String message) {
		return of(code, env, message, null);
	}

	public static GraphQLException of(ErrorCode code, DataFetchingEnvironment env, String message, Object data) {
		return new GraphQLException(code.construct(env, message), data);
	}
}
