package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901;


import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;

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
	private FagomradeCode tema; //Todo: endre dette feltet til fagomrade
	private LocalDateTime datoOpprettet;
	private String mottakskanal;
	private String avsenderMottakerId;
	private TilgangBrukerDto bruker;
	private TilgangSakDto sak;
	private SkjermingTypeCode skjerming;
	private TilgangDokumentInfoDto dokument;
}
