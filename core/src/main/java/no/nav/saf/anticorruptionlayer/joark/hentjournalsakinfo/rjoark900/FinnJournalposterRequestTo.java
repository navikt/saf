package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900;

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
public class FinnJournalposterRequestTo {
	private final List<String> gsakSakIds;
	private final List<String> psakSakIds;
	private final String fraDato;
	private final List<JournalStatusCode> inkluderJournalStatus;
	private final List<JournalpostTypeCode> inkluderJournalpostType;
	private final boolean visFeilregistrerte;
	private final List<String> alleIdenter;
	private final Integer foerste;
	private final String etterPeker;
	private final Integer siste;
	private final String foerPeker;
}
