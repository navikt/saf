package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangSak {
	private final String aktoerId;
	private final String foedselsnummer;
	private final String orgnummer;
	private final String arkivsaksnummer;
	private final String arkivsaksystem;
	private final String fagsaksnummer;
	private final String fagsaksystem;
	private final String tema;
	/**
	 * Forvaltningslovens § 19 "innskrenkret adgang til visse slags opplysninger"
	 */
	private final boolean paragraf19;
	private final List<TilgangRelevantTredjepart> relevanteTredjeparter;
}
