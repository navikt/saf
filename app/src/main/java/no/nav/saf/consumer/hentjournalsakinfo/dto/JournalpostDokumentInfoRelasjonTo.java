package no.nav.saf.consumer.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.consumer.hentjournalsakinfo.dto.kode.TilknyttetJournalpostSomCode;

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
