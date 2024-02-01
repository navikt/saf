package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark901;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;

@Value
@Builder
public class TilgangVariantDto {
	private VariantFormatCode variantFormat;
	private SkjermingTypeCode skjerming;
}
