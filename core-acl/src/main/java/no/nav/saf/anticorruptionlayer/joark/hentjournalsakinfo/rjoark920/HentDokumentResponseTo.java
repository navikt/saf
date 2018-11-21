package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark920;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.MediaType;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
public class HentDokumentResponseTo {

	private final String dokument;
	private final MediaType mediaType;
}
