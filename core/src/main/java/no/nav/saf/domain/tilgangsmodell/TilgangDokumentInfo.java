package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Skjerming;

import java.util.List;

@Value
@Builder
public class TilgangDokumentInfo {

	/**
	 * Attributter brukt for tilgangskontroll
	 **/
	Skjerming skjerming;
	List<TilgangDokumentvariant> tilgangDokumentvarianter;

	/**
	 * Attributter brukt for å lage en unik id ifm. caching av tilgangskontrollresultat.
	 * Kun relevant for tilgangsmodellen til dokumentoversiktene
	 **/
	String journalpostId;
	String dokumentInfoId;
}
