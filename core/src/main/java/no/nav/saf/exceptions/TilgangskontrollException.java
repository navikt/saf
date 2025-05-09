package no.nav.saf.exceptions;

import lombok.Getter;
import no.nav.saf.graphql.ErrorCode;
import no.nav.saf.tilgangskontroll.pep.PepAnswer;
import no.nav.saf.tilgangskontroll.pep.reasons.AbacDenyReason;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@Getter
@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class TilgangskontrollException extends SafFunctionalException {
	public static final String REASON_CODE = "reason_code";
	public static final String REASON_MESSAGE = "reason_message";

	protected final String denyReason;
	protected final AbacDenyReason pepDenyReason;

	public TilgangskontrollException(String message, PepAnswer pepAnswer) {
		super("Avvist av SAF tilgangskontroll: " + message, ErrorCode.FORBIDDEN);
		this.denyReason = pepAnswer.getDenyReasonSporing();
		this.pepDenyReason = pepAnswer.getPepDenyReason();
	}

	@Override
	public Map<String, Object> getExtensions() {
		var map = new HashMap<>(super.getExtensions());
		map.put(REASON_CODE, pepDenyReason.getAbacDenyReasonCode().code);
		map.put(REASON_MESSAGE, pepDenyReason.getHumanReadableDenyReason());
		return map;
	}
}
