package no.nav.saf.tilgangskontroll.pep.reasons;

import no.nav.saf.tilgangskontroll.pep.DenyReasonCode;

import java.util.Map;

import static java.util.Collections.emptyMap;

public final class JournalstatusReason extends DenyReason {

	public JournalstatusReason() {
		this(emptyMap());
	}

	public JournalstatusReason(Map<String, String> advices) {
		super(advices, DenyReasonCode.JOURNALSTATUS);
	}

	public String getHumanReadableDenyReason() {
		return "Du har ikke tilgang til journalpost / dokument fordi den har status Utgår eller Ukjent Bruker." + FAGPOST;
	}
}