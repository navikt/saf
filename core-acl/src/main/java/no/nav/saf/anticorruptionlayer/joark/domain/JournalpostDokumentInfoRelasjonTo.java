package no.nav.saf.anticorruptionlayer.joark.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.TilknyttetJournalpostSomCode;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalpostDokumentInfoRelasjonTo {

	private Long journalpostDokumentInfoRelasjonId;
	private String tilknyttetAvNavn;
	private TilknyttetJournalpostSomCode tilknyttetJournalpostSom;
	private DokumentInfoTo dokumentInfo;

}
