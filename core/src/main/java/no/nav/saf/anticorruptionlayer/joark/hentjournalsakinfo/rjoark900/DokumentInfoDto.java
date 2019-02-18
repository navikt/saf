package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.DokumentStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;

import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DokumentInfoDto {
	private String dokumentInfoId;
	private DokumentStatusCode dokumentstatus;
	private String brevkode;
	private String tittel;
	private SkjermingTypeCode skjerming;
	private List<VariantDto> varianter;
	private Long origJournalpostId;
	private List<LogiskVedleggDto> logiske;
}
