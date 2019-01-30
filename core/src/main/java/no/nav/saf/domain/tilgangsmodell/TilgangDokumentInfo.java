package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Skjerming;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangDokumentInfo {

	/**
	 * Attributter brukt for tilgangskontroll
	 **/
	private final Skjerming skjerming;
	private final List<TilgangDokumentvariant> tilgangDokumentvarianter;

	/**
	 * Attributter brukt for å lage en unik id ifm. caching av tilgangskontrollresultat.
	 * Kun relevant for tilgangsmodellen til dokumentoversiktene
	 **/
	private final String journalpostId;
	private final String dokumentInfoId;
}
