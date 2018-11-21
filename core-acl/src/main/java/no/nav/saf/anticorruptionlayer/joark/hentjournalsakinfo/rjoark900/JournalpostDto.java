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
	private String journalForendeEnhetId;
	private Date journalDato;
	private Date sendtPrintDato;
	private String innhold;
	private FagomradeCode fagomrade;
	private JournalStatusCode journalstatus;
	private Date dokumentDato;
	private String avsenderMottakerNavn;
	private String journalfortAvNavn;
	private Date mottattDato;
	private MottaksKanalCode mottakskanal;
	private UtsendingsKanalCode utsendingskanal;
	private Date ekspedertDato;
	private Date lestDato;
	private Date mottattAdressatDato;
	private JournalpostTypeCode journalposttype;
	private SaksrelasjonDto saksrelasjon;
	private Date datoOpprettet;
	private List<DokumentInfoDto> dokumenter;
}
