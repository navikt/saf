package no.nav.saf.domain.tilgangsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tilgangskontroll.abstraction.SecModel;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class TilgangSak implements SecModel {
	private final String aktoerId;
	private final String arkivsaksnummer;
	private final String arkivsaksystem;
	private final String sakspart;
	private final List<TilgangRelevantTredjepart> relevanteTredjeparter;
	private final String tema;
}
