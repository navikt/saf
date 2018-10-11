package no.nav.saf.consumer.hentjournalsakinfo;

import lombok.Builder;
import lombok.Data;
import no.nav.saf.consumer.hentjournalsakinfo.dto.JournalpostTo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class HentJournalposterResponse {
	private final List<JournalpostTo> gsakJournalpostList = new ArrayList<>();
	private final List<JournalpostTo> psakJournalpostList = new ArrayList<>();
}
