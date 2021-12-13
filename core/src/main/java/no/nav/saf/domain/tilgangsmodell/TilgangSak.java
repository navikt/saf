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
	// Kjerneattributter brukt for tilgangskontroll
	String aktoerId;
	String foedselsnummer;
	String orgnummer;
	Tema tema;
	String fagsaksystem;

	List<TilgangRelevantTredjepart> relevanteTredjeparter; // Bisys

	List<String> fpAktoerIdList;
	List<String> k9AktoerIdList;

	// Ekstra attributter for å forenkle kodeflyt
	String arkivsaksnummer;
	Arkivsakssystem arkivsaksystem;
}
