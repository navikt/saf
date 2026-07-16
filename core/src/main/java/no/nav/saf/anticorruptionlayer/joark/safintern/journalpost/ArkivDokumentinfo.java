package no.nav.saf.anticorruptionlayer.joark.safintern.journalpost;

import lombok.Builder;
import no.nav.saf.anticorruptionlayer.joark.ArkivJournalpostMapper;
import no.nav.safselvbetjening.tilgang.TilgangDokument;
import no.nav.safselvbetjening.tilgang.TilgangSkjermingType;

import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record ArkivDokumentinfo(
		// brukt til tilgangskontroll i hentdokument
		Long dokumentInfoId,
		String skjerming,
		//
		String brevkode,
		String dokumenttypeId,
		OffsetDateTime ferdigDato,
		Boolean kassert,
		String kategori,
		List<ArkivLogiskVedlegg> logiskVedlegg,
		Long originalJournalpostId,
		Boolean sensitivt,
		String status,
		String tilknyttetSom,
		String tittel,
		Integer rekkefoelge,
		List<ArkivFildetaljer> fildetaljer
) {

	public TilgangDokument getTilgangDokument() {
		return TilgangDokument.builder()
				.id(dokumentInfoId)
				.kassert(kassert != null && kassert)
				.kategori(kategori)
				.hoveddokument(ArkivJournalpostMapper.TILKNYTTET_SOM_HOVEDDOKUMENT.equals(tilknyttetSom))
				.skjerming(TilgangSkjermingType.from(skjerming))
				.dokumentvarianter(fildetaljer.stream().map(ArkivFildetaljer::getTilgangVariant).toList())
				.build();
	}
}
