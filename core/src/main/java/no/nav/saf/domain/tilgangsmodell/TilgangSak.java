package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangSak {
	//Kjerneattrubutter brukt for tilgangskontroll
	private final String aktoerId;
	private final String foedselsnummer;
	private final String orgnummer;
	private final Tema tema;
	private final String fagsaksystem;

	private final List<TilgangRelevantTredjepart> relevanteTredjeparter;

	private final List<String> fpAktoerIdList;

	//Ekstra attributter for å forenkle kodeflyt
	private final String arkivsaksnummer;
	private final Arkivsakssystem arkivsaksystem;
}
