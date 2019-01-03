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
	private final List<TilgangRelevantTredjepart> relevanteTredjeparter;
	private final String tema;
}
