package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900;

import lombok.Data;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
public class FinnJournalposterResponseTo {
	private List<JournalpostDto> tilgangJournalposter;
}
