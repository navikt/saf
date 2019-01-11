package no.nav.saf.hentdokument;

import lombok.Builder;
import lombok.Value;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
public class HentDokumentArguments {

	private final String journalpostId;
	private final String dokumentInfoId;
	private final String variantFormat;

}
