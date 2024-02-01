package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900;

import lombok.Data;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;

import java.util.List;

@Data
public class FinnJournalposterResponseTo {
	private List<JournalpostDto> tilgangJournalposter;
}
