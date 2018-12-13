package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagomradeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.MottaksKanalCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.UtsendingsKanalCode;

import java.util.Date;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@NoArgsConstructor
public class JournalpostDto {
	private Long journalpostId;
	private Long prevJournalpostId;
	private Long nextJournalpostId;
	private Long totaltAntall;
	private String journalForendeEnhetId;
	private String innhold;
	private FagomradeCode fagomrade;
	private JournalStatusCode journalstatus;
	private String avsenderMottakerNavn;
	private String journalfortAvNavn;
	private MottaksKanalCode mottakskanal;
	private UtsendingsKanalCode utsendingskanal;
	private JournalpostTypeCode journalposttype;
	private SaksrelasjonDto saksrelasjon;
	private Date datoOpprettet;
	private Date mottattDato;
	private Date journalDato;
	private Date dokumentDato;
	private Date avsReturDato;
	private Date sendtPrintDato;
	private Date ekspedertDato;
	private List<DokumentInfoDto> dokumenter;
}
