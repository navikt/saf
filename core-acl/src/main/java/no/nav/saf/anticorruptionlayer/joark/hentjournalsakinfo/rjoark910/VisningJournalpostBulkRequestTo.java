package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark910;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@AllArgsConstructor
public class VisningJournalpostBulkRequestTo {
	private List<String> journalpostIds = new ArrayList<>();
}
