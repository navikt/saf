package no.nav.saf.hentdokument;

import lombok.Builder;
import lombok.Value;

/**
 * Representerer en linje i sporingsloggen.
 * Typisk en linje per kall til hentdokument.
 */
@Value
@Builder
class HentDokumentSporingLogglinje {
	public static final String BESLUTNING_PERMIT = "PERMIT";
	public static final String BESLUTNING_DENY = "DENY";

	String brukerId;
	String navIdent;
	String tilgangsbeslutning;
	String begrunnelse;
	String journalpostId;
	String dokumentInfoId;
	String variantformat;
	String tema;

	@Override
	public String toString() {
		return "brukerId=" + brukerId +
			   ", navIdent=" + navIdent +
			   ", tilgangsbeslutning=" + tilgangsbeslutning +
			   (BESLUTNING_DENY.equals(tilgangsbeslutning) ? ", begrunnelse=\"" + begrunnelse + "\"" : "") +
			   ", journalpostId=" + journalpostId +
			   ", dokumentInfoId=" + dokumentInfoId +
			   ", variantformat=" + variantformat +
			   ", tema=" + tema;
	}
}
