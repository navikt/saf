package no.nav.saf.tilgangskontroll;

import static org.apache.commons.lang3.StringUtils.substringAfterLast;

import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;

import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 * <p>
 * NB! Be sure to know what you are doing before changing any of these methods, as audit logging is expected to conform to a specific format!
 */
public abstract class AbacLogger {

	static final String SEPARATOR = ".";

	public abstract void logAbacDeny(final XacmlRequest xacmlRequest, final XacmlResponse xacmlResponse);

	public abstract void logAbacPermit(final XacmlRequest xacmlRequest, final XacmlResponse xacmlResponse);

	String mapRequest(final XacmlRequest xacmlRequest) {
		return "Authorization Request: " +
				xacmlRequest.getResources()
						.stream()
						.map(xacmlAttribute -> substringAfterLast(xacmlAttribute.getAttributeId(), SEPARATOR) + "=" + xacmlAttribute
								.getValue())
						.collect(Collectors.joining(", "));
	}

	String mapResponse(final XacmlResponse xacmlResponse) {
		return "Authorization Response: " +
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
}
