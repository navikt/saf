package no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900;

import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.DokumentStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@NoArgsConstructor
public class DokumentInfoDto {
	private String dokumentInfoId;
	private DokumentStatusCode dokumentstatus;
	private String brevkode;
	private VariantFormatCode variantFormat;
	private String tittel;
}
