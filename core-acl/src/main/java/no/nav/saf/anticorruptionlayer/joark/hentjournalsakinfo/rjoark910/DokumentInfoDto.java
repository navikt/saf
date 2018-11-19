package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark910;

import lombok.Data;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
public class DokumentInfoDto {
	private Long dokumentInfoId;
	private String tittel;
	private Long originalJournalpostId;
}
