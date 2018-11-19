package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark910;

import lombok.Data;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
public class SaksrelasjonDto {
	private String sakId;
	private Boolean feilregistrert;
	private FagsystemCode fagsystem;
}
