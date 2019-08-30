package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark904;

import lombok.Builder;
import lombok.Data;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalpostTypeCode;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class FinnJournalposterStatusRequestTo {
	private JournalStatusCode journalstatus;
	private String fraDato;
	private List<JournalpostTypeCode> journalposttyper;
	private Integer foerste;
	private String etterPeker;
}
