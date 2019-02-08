package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Skjerming;
import no.nav.saf.domain.kode.Variantformat;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangDokumentvariant {

	/**
	 * Attributter brukt for tilgangskontroll
	 **/
	private final Variantformat variantformat;
	private final Skjerming skjerming;

	/**
	 * Attributter brukt for å lage en unik id ifm. caching av tilgangskontrollresultat.
	 * Kun relevant for tilgangsmodellen til dokumentoversiktene
	 **/
	private final String journalpostId;
	private final String dokumentInfoId;

}
