package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;

public abstract class StandardAbacBackedPep<T> extends Pep<T> {

	protected PepAnswer mapToAbacAnswer(XacmlResponse xacmlResponse) {
		return xacmlResponse.isPermit() ? PepAnswer.permit() : translateToDenyReasonCode(xacmlResponse);
	}

	protected abstract PepAnswer translateToDenyReasonCode(XacmlResponse xacmlResponse);

}
