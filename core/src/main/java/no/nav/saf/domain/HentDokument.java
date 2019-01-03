package no.nav.saf.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.http.MediaType;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HentDokument {
	@Builder.Default
	private final byte[] dokument = new byte[0];
	@Builder.Default
	private final MediaType mediaType = MediaType.TEXT_PLAIN;

}
