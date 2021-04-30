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
	private final String sakId;
	private final String fagsaksystem;
	/**
	 * Forvaltningslovens § 19 "innskrenkret adgang til visse slags opplysninger"
	 */
	private final Boolean paragraf19;
	private final List<TilgangRelevantTredjepart> relevanteTredjeparter;

	//Ekstra attributter for å forenkle kodeflyt
	private final String arkivsaksnummer;
	private final Arkivsakssystem arkivsaksystem;
}
