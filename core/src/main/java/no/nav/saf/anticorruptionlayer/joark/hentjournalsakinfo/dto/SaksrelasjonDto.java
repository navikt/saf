package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
@AllArgsConstructor
public class SaksrelasjonDto {
	private String sakId;
	private Boolean feilregistrert;
	private FagsystemCode fagsystem;
}
