package no.nav.saf.exceptions;

import lombok.Getter;
import no.nav.saf.graphql.ErrorCode;
import no.nav.saf.tilgangskontroll.pep.AbacAnswer;
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
	protected final AbacDenyReason abacDenyReason;

	public TilgangskontrollException(String message, AbacAnswer abacAnswer) {
		super("Avvist av SAF tilgangskontroll: " + message, ErrorCode.FORBIDDEN);
		this.denyReason = abacAnswer.getDenyReasonSporing();
		this.abacDenyReason = abacAnswer.getAbacDenyReason();
	}

	@Override
	public Map<String, Object> getExtensions() {
		var map = new HashMap<>(super.getExtensions());
		map.put(REASON_CODE, abacDenyReason.getAbacDenyReasonCode().code);
		map.put(REASON_MESSAGE, abacDenyReason.getHumanReadableDenyReason());
		return map;
	}
}
