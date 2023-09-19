package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto;

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
	private String filuuid;
	private String filtype;
	private String filstorrelse;
	private SkjermingTypeCode skjerming;
}
