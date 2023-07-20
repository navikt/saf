package no.nav.saf.anticorruptionlayer.joark.safintern.hentdokument;

import org.springframework.http.MediaType;

public record HentDokumentResponseTo(byte[] dokument, MediaType mediaType) {
	@Override
	public String toString() {
		return "HentDokumentResponseTo{" +
			   "length=" + dokument.length +
			   ", mediaType=" + mediaType +
			   '}';
	}
}
