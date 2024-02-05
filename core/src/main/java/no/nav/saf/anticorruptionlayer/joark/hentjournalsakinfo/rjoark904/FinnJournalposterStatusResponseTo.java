package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark904;

import lombok.Data;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;

import java.util.List;

@Data
public class FinnJournalposterStatusResponseTo {
	private final List<JournalpostDto> tilgangJournalposter;
}
