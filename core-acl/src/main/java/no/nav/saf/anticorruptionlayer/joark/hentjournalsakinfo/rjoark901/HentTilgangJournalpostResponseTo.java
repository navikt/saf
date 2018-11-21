package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HentTilgangJournalpostResponseTo {

	private TilgangJournalpostDto tilgangJournalpostDto;
}
