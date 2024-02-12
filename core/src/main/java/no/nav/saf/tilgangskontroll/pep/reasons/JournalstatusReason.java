package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.AbacAnswer;

import java.util.Map;

public final class JournalstatusReason extends AbacDenyReason {

	public JournalstatusReason(Map<String,String> advices) {
		super(advices, AbacAnswer.AbacDenyReasonCode.JOURNALSTATUS);
	}
}