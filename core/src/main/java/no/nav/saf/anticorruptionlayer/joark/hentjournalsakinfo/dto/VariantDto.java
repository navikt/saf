package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.safselvbetjening.tilgang.TilgangSkjermingType;
import no.nav.safselvbetjening.tilgang.TilgangVariant;
import no.nav.safselvbetjening.tilgang.TilgangVariantFormat;

@Value
@Builder
public class VariantDto {
	VariantFormatCode variantf;
	String filnavn;
	String filuuid;
	String filtype;
	String filstorrelse;
	SkjermingTypeCode skjerming;

	public TilgangVariant getTilgangVariant() {
		return TilgangVariant.builder()
				.variantformat(TilgangVariantFormat.from(variantf == null ? null : variantf.name()))
				.skjerming(TilgangSkjermingType.from(skjerming == null ? null : skjerming.name()))
				.build();
	}
}
