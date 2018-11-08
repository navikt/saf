package no.nav.saf.tjeneste.hentdokument;

import lombok.Builder;
import lombok.Value;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
public class HentDokumentArguments {

	private final String journalpostId;
	private final String dokumentId;
	private final String variantFormat;
	private final String aktoerId = "***gammelt_fnr***71";

}
