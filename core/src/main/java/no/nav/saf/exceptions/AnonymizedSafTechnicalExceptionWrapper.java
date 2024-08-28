package no.nav.saf.exceptions;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.GraphqlErrorHelper;
import graphql.language.SourceLocation;

import java.util.List;
import java.util.Map;

import static graphql.ErrorType.DataFetchingException;
import static java.util.Collections.emptyList;

public class AnonymizedSafTechnicalExceptionWrapper implements GraphQLError {
	private final SafTechnicalException safTechnicalException;

	AnonymizedSafTechnicalExceptionWrapper(SafTechnicalException safTechnicalException) {
		this.safTechnicalException = safTechnicalException;
	}

	@Override
	public Map<String, Object> getExtensions() {
		return safTechnicalException.getExtensions();
	}

	@Override
	public String getMessage() {
		return safTechnicalException.getMessage();
	}

	@Override
	public List<SourceLocation> getLocations() {
		return emptyList();
	}

	@Override
	public ErrorClassification getErrorType() {
		return DataFetchingException;
	}

	@Override
	public Map<String, Object> toSpecification() {
		Map<String, Object> specification = GraphqlErrorHelper.toSpecification(this);
		specification.put("exceptionType", "TECHNICAL");
		specification.put("exception", safTechnicalException.getClass().getSimpleName());
		return specification;
	}
}
