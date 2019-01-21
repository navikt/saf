package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901;


import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;

import java.time.LocalDateTime;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@NoArgsConstructor
public class TilgangJournalpostDto {

	private String journalpostId;
	private JournalStatusCode journalStatus;
	private JournalpostTypeCode journalpostType;
	private String tema;
	private LocalDateTime datoOpprettet;
	private String mottakskanal;
	private String avsenderMottakerId;
	private TilgangBrukerDto bruker;
	private TilgangSakDto sak;
	private TilgangDokumentInfoDto dokument;
}
