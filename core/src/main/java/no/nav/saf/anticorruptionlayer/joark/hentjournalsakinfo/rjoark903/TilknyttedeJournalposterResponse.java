package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark903;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;

import java.util.ArrayList;
import java.util.List;

@Value
public class TilknyttedeJournalposterResponse {
	private final List<JournalpostDto> tilknyttedeJournalposter;

	public TilknyttedeJournalposterResponse(@JsonProperty("tilknyttedeJournalposter") List<JournalpostDto> tilknyttedeJournalposter) {
		this.tilknyttedeJournalposter = new ArrayList<>(tilknyttedeJournalposter);
	}
}
