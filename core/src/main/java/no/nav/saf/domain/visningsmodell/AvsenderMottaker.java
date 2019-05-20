package no.nav.saf.domain.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.AvsenderMottakerIdTypeCode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
@Builder
public class AvsenderMottaker {
	private final String id;
	private final AvsenderMottakerIdTypeCode idType;
	private final String navn;
	private final String land;
	private final boolean erLikBruker;
}
