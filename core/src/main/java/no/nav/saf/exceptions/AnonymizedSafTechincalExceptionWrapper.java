package no.nav.saf.exceptions;

import graphql.ErrorClassification;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

import java.util.List;
import java.util.Map;

public class AnonymizedSafTechincalExceptionWrapper implements GraphQLError {
	private final SafTechnicalException safTechnicalException;

	AnonymizedSafTechincalExceptionWrapper(SafTechnicalException safTechnicalException) {
		this.safTechnicalException = safTechnicalException;
	}

	@Override
	public String getMessage() {
		return "Teknisk feil. Prøv igjen senere.";
	}

	@Override
	public List<SourceLocation> getLocations() {
		return safTechnicalException.getLocations();
	}

	@Override
	public ErrorClassification getErrorType() {
		return safTechnicalException.getErrorType();
	}

	@Override
	public Map<String, Object> getExtensions() {
		return safTechnicalException.getExtensions();
	}
}
