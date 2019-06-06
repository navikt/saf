package no.nav.saf.tilgangskontroll.abac.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
@AllArgsConstructor
public class XacmlResponse {
	private static final XacmlResponse PERMIT = new XacmlResponse(Decision.PERMIT, Decision.PERMIT, Collections.emptyList(), Collections.emptyList());
	private static final XacmlResponse DENY = new XacmlResponse(Decision.DENY, Decision.DENY, Collections.emptyList(), Collections.emptyList());

	private final Decision decision;
	private final Decision originalDecision;
	private final List<Obligation> obligations;
	private final List<Advice> advices;

	public boolean isPermit() {
		return Decision.PERMIT.equals(decision);
	}

	public boolean isDeny() {
		return Decision.DENY.equals(decision);
	}

	public static XacmlResponse permit() {
		return PERMIT;
	}

	public static XacmlResponse deny() {
		return DENY;
	}
}