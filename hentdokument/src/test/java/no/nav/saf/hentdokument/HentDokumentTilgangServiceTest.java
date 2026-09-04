package no.nav.saf.hentdokument;

import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivDokumentinfo;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivFildetaljer;
import no.nav.saf.exceptions.DokumentIkkeFunnetException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.ARKIV;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.ORIGINAL;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.SLADDET;
import static no.nav.saf.util.MDCConstants.DOKUMENT_INFO_ID;
import static no.nav.saf.util.MDCConstants.JOURNALPOST_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class HentDokumentTilgangServiceTest {

	@Test
	void shouldReturnSladdetOverArkivVariant() {
		ArkivDokumentinfo dokument = dokumentMedVarianter(ARKIV, SLADDET);

		VariantFormatCode valgtVariantFormat = HentDokumentTilgangService.velgVariantformat(JOURNALPOST_ID, DOKUMENT_INFO_ID, null, dokument);

		assertThat(valgtVariantFormat).isEqualTo(SLADDET);
	}

	@Test
	void shouldReturnEksplisittValgtVariant() {
		ArkivDokumentinfo dokument = dokumentMedVarianter(ARKIV, SLADDET);

		VariantFormatCode valgtVariantFormat = HentDokumentTilgangService.velgVariantformat(JOURNALPOST_ID, DOKUMENT_INFO_ID, ARKIV, dokument);

		assertThat(valgtVariantFormat).isEqualTo(ARKIV);
	}

	@Test
	void shouldThrowWhenEksplisittValgtVariantIkkeFinnes() {
		ArkivDokumentinfo dokument = dokumentMedVarianter(ARKIV);

		assertThatExceptionOfType(DokumentIkkeFunnetException.class)
				.isThrownBy(() -> HentDokumentTilgangService.velgVariantformat(JOURNALPOST_ID, DOKUMENT_INFO_ID, ORIGINAL, dokument))
				.withMessageContaining("Dokument med journalpostId=%s, dokumentInfoId=%s, variantFormat=%s ikke funnet i Joark."
						.formatted(JOURNALPOST_ID, DOKUMENT_INFO_ID, ORIGINAL));
	}

	@Test
	void shouldThrowWhenHverkenSladdetEllerArkivErTilgjengelig() {
		ArkivDokumentinfo dokument = dokumentMedVarianter(ORIGINAL);

		assertThatExceptionOfType(DokumentIkkeFunnetException.class)
				.isThrownBy(() -> HentDokumentTilgangService.velgVariantformat(JOURNALPOST_ID, DOKUMENT_INFO_ID, null, dokument))
				.withMessageContaining("Dokument med journalpostId=%s og dokumentInfoId=%s har ingen SLADDET- eller ARKIV-variant for automatisk valg i Joark."
						.formatted(JOURNALPOST_ID, DOKUMENT_INFO_ID));
	}

	private static ArkivDokumentinfo dokumentMedVarianter(VariantFormatCode... variantformater) {
		return ArkivDokumentinfo.builder()
				.fildetaljer(Arrays.stream(variantformater)
						.map(variantformat -> ArkivFildetaljer.builder().format(variantformat.name()).build())
						.toList())
				.build();
	}
}
