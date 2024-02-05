package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TilgangDokumentInfoDto {
	private String dokumentinfoId;
	private String dokumentstatus;
	private String brevkode;
	private SkjermingTypeCode skjerming;
	private TilgangVariantDto variant;

}
