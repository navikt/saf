package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;

public abstract class StandardPep<T> extends Pep<T> {

	protected AbacAnswer mapXacmlResponse(XacmlResponse xacmlResponse) {
		return xacmlResponse.isPermit() ? AbacAnswer.permit() : translateToDenyReasonCode(xacmlResponse);
	}

	protected abstract AbacAnswer translateToDenyReasonCode(XacmlResponse xacmlResponse);

}
