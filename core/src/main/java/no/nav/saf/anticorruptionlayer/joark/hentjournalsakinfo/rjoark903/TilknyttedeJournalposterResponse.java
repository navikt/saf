package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark903;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class TilknyttedeJournalposterResponse {
	private final List<JournalpostDto> tilknyttedeJournalposter;

	public TilknyttedeJournalposterResponse(@JsonProperty("tilknyttedeJournalposter") List<JournalpostDto> tilknyttedeJournalposter) {
		this.tilknyttedeJournalposter = tilknyttedeJournalposter;
	}
}
