package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.DokumentStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivFildetaljer;
import no.nav.safselvbetjening.tilgang.TilgangDokument;
import no.nav.safselvbetjening.tilgang.TilgangSkjermingType;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DokumentInfoDto {
	private String dokumentInfoId;
	private String tilknyttetSom;
	private DokumentStatusCode dokumentstatus;
	private Date datoFerdigstilt;
	private String brevkode;
	private String dokumenttypeId;
	private String tittel;
	private SkjermingTypeCode skjerming;
	private List<VariantDto> varianter;
	private Long origJournalpostId;
	private Boolean sensitivt;
	private Integer rekkefoelge;
	private List<LogiskVedleggDto> logiske;
	private Boolean kassert;
	private String kategori;

	public TilgangDokument getTilgangDokument(boolean isFirst) {
		return TilgangDokument.builder()
				.id(Long.parseLong(dokumentInfoId))
				.kassert(kassert != null && kassert)
				.kategori(kategori)
				.hoveddokument(isFirst)
				.skjerming(TilgangSkjermingType.from(skjerming == null ? null : skjerming.name()))
				.dokumentvarianter(varianter.stream().map(VariantDto::getTilgangVariant).toList())
				.build();
	}
}
