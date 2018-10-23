package no.nav.saf.domain.tilgangsmodell;

import lombok.Value;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class TilgangSak {
	private final String saksnummer;
	private final String arkivsaksystem;
	private final String sakspart;
	private final List<TilgangRelevantTredjepart> relevanteTredjeparter;
	private final String tema;
}
