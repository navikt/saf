package no.nav.saf.domain;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.MediaType;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
public class HentDokument {
	@Builder.Default
	private final byte[] dokument = new byte[0];
	@Builder.Default
	private final MediaType mediaType = MediaType.TEXT_PLAIN;
	@Builder.Default
	private final String extension = "";
}
