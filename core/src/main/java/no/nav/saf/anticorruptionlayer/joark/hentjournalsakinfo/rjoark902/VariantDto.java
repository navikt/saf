package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark902;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Value
@Builder
public class VariantDto {
	private VariantFormatCode variantf;
	private String filnavn;
	private SkjermingTypeCode skjerming;
}
