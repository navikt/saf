package no.nav.saf.domain;

import lombok.Builder;
import lombok.Value;
import org.springframework.http.MediaType;

@Value
@Builder
public class HentDokument {
	@Builder.Default
	byte[] dokument = new byte[0];
	@Builder.Default
	MediaType mediaType = MediaType.TEXT_PLAIN;
	@Builder.Default
	String extension = "";
	String variantFormat;
}
