package no.nav.saf.hentdokument;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivDokumentinfo;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivFildetaljer;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.ARKIV;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.ORIGINAL;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.SLADDET;
import static org.assertj.core.api.Assertions.assertThat;

class HentDokumentTilgangServiceTest {

	@Test
	void shouldReturnSladdetOverArkivVariant() {
		ArkivDokumentinfo dokument = dokumentMedVarianter(ARKIV, SLADDET);

		VariantFormatCode valgtVariantFormat = HentDokumentTilgangService.velgVariantFormat(null, dokument);

		assertThat(valgtVariantFormat).isEqualTo(SLADDET);
	}

	@Test
	void shouldReturnEkplisittValgtVariant() {
		ArkivDokumentinfo dokument = dokumentMedVarianter(ARKIV, SLADDET);

		VariantFormatCode valgtVariantFormat = HentDokumentTilgangService.velgVariantFormat(ARKIV, dokument);

		assertThat(valgtVariantFormat).isEqualTo(ARKIV);
	}

	@Test
	void shouldReturnNullWhenHverkenSladdetEllerArkivErTilgjengelig() {
		ArkivDokumentinfo dokument = dokumentMedVarianter(ORIGINAL);

		VariantFormatCode valgtVariantFormat = HentDokumentTilgangService.velgVariantFormat(null, dokument);

		assertThat(valgtVariantFormat).isNull();
	}

	private static ArkivDokumentinfo dokumentMedVarianter(VariantFormatCode... variantformater) {
		return ArkivDokumentinfo.builder()
				.fildetaljer(Arrays.stream(variantformater)
						.map(variantformat -> ArkivFildetaljer.builder().format(variantformat.name()).build())
						.toList())
				.build();
	}
}
