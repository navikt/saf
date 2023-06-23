package no.nav.saf.tilgangskontroll;

import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;

import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

/**
 * NB! Be sure to know what you are doing before changing any of these methods, as audit logging is expected to conform to a specific format!
 */
public abstract class AbacLogger {

	private static final String SEPARATOR = ".";

	public abstract void logAbacDeny(final XacmlRequest xacmlRequest, final XacmlResponse xacmlResponse);

	public abstract void logAbacPermit(final XacmlRequest xacmlRequest, final XacmlResponse xacmlResponse);

	String mapRequest(final XacmlRequest xacmlRequest) {
		return "Authorization Request: " +
			   xacmlRequest.getResources()
					   .stream()
					   .map(xacmlAttribute -> substringAfterLast(xacmlAttribute.getAttributeId(), SEPARATOR) + "=" + maskValue(xacmlAttribute.getValue()))
					   .collect(Collectors.joining(", "));
	}

	String mapResponse(final XacmlResponse xacmlResponse) {
		return " Authorization Response: " +
			   "decision=" + xacmlResponse.getDecision().getValue() +
			   (
					   xacmlResponse.getAdvices().isEmpty() ? "" :
							   (", " + xacmlResponse.getAdvices()
									   .stream()
									   .flatMap(a -> a.getAttributeAssignments().stream())
									   .map(as -> substringAfterLast(as.getAttributeId(), SEPARATOR) + "=" + as.getValue())
									   .collect(Collectors.joining(", ")))
			   );
	}

	private static String maskValue(String value) {
		return switch (value.length()) {
			case 11 -> "***********";
			case 13 -> "*************";
			default -> value;
		};
	}
}
