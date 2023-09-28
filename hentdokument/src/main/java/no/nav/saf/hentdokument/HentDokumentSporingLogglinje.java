package no.nav.saf.hentdokument;

import lombok.Builder;
import lombok.Value;

/**
 * Representerer en linje i sporingsloggen.
 * Typisk en linje per kall til hentdokument.
 */
@Value
@Builder
public class HentDokumentSporingLogglinje {
	public static final String BESLUTNING_PERMIT = "PERMIT";
	public static final String BESLUTNING_DENY = "DENY";

	private final String brukerId;
	private final String navIdent;
	private final String tilgangsbeslutning;
	private final String begrunnelse;
	private final String journalpostId;
	private final String dokumentInfoId;
	private final String variantformat;
	private final String tema;

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
