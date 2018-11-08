package no.nav.saf.tjeneste.hentdokument;

import lombok.Builder;
import lombok.Value;

import java.util.Base64;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
public class HentDokumentResponse {

	private final Base64 dokument;
	private final String mimetype;

}
