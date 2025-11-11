package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Skjerming;
import no.nav.saf.domain.kode.Variantformat;

@Value
@Builder
public class TilgangDokumentvariant {

	/**
	 * Attributter brukt for tilgangskontroll
	 **/
	Variantformat variantformat;
	Skjerming skjerming;

	/**
	 * Attributter brukt for å lage en unik id ifm. caching av tilgangskontrollresultat.
	 * Kun relevant for tilgangsmodellen til dokumentoversiktene
	 **/
	String journalpostId;
	String dokumentInfoId;

}
