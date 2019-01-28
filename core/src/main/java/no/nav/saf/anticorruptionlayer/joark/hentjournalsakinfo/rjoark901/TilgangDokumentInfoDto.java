package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.VariantDto;

import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TilgangDokumentInfoDto {
	private String dokumentinfoId;
	private String dokumentstatus;
	private String brevkode;
	private List<VariantDto> varianter;
	private SkjermingTypeCode skjerming;

}
