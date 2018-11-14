package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.MottaksKanalCode;

import java.util.Date;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@NoArgsConstructor
public class TilgangJournalpostDto {
	private String journalpostId;
	private JournalStatusCode journalStatus;
	private JournalpostTypeCode journalpostType;
	private FagomradeCode fagomrade;
	private Date datoOpprettet;
	private MottaksKanalCode mottakskanal;
	private String avsenderMottakerId;
	private List<TilgangDokumentInfoDto> dokumenter;
}
