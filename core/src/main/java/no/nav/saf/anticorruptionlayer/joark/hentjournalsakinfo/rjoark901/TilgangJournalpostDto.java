package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@NoArgsConstructor
public class TilgangJournalpostDto {

	private String journalpostId;
	private String journalStatus;
	private String journalpostType;
	private String tema;
	private LocalDateTime datoOpprettet;
	private String mottakskanal;
	private String avsenderMottakerId;
	//	private  Boolean kvalitetssikretForInnsyn; TODO Finne ut av denne
	private TilgangBrukerDto bruker;
	private TilgangSakDto sak;
	private TilgangDokumentInfoDto dokument;
}
