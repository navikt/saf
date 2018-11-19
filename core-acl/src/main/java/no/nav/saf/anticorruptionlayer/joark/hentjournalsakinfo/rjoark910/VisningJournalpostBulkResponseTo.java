package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark910;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
public class VisningJournalpostBulkResponseTo {
	private List<JournalpostDto> journalposter = new ArrayList<>();
}
