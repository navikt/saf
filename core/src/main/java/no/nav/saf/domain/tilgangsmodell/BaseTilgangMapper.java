package no.nav.saf.domain.tilgangsmodell;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivBruker;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivFildetaljer;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.domain.kode.Skjerming;
import no.nav.saf.domain.kode.Tema;

import java.util.List;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.BrukerTypeCode.ORGANISASJON;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.BrukerTypeCode.PERSON;
import static org.apache.commons.lang3.StringUtils.trim;

@Slf4j
public class BaseTilgangMapper {
	public static TilgangBruker mapTilgangBrukerUtenTilknyttetSak(ArkivJournalpost arkivJournalpost) {
		ArkivBruker bruker = arkivJournalpost.bruker();
		if (bruker == null || bruker.type() == null) {
			return null;
		}
		return switch (bruker.type()) {
			case PERSON -> TilgangBruker.builder()
					.foedselsnummer(bruker.id())
					.build();
			case ORGANISASJON -> TilgangBruker.builder()
					.orgnummer(trim(bruker.id()))
					.build();
			default -> {
				log.warn("Forventet bruker.type=(PERSON, ORGANISASJON) for journalpost uten sakstilknytning med journalpostId={}. Fikk bruker.type={}", arkivJournalpost.journalpostId(), bruker.type());
				yield null;
			}
		};
	}


	public static TilgangSak mapTilgangSakUtenSakstilknytning(ArkivJournalpost arkivJournalpost) {
		return TilgangSak.builder()
				.tema(Tema.valueOf(arkivJournalpost.fagomraade()))
				.avsluttet(false)
				.build();
	}

	public static TilgangJournalpost mapTilgangJournalpost(ArkivJournalpost arkivJournalpost) {
		Long journalpostId = arkivJournalpost.journalpostId();
		return TilgangJournalpost.builder()
				.journalpostId(journalpostId)
				.journalstatus(JournalStatusCode.valueOf(arkivJournalpost.status()).toSafJournalstatus())
				.skjerming(mapSkjerming(arkivJournalpost.skjerming()))
				.dokumenter(mapTilgangDokumentInfo(arkivJournalpost))
				.build();
	}

	public static List<TilgangDokumentInfo> mapTilgangDokumentInfo(ArkivJournalpost arkivJournalpost) {
		Long journalpostId = arkivJournalpost.journalpostId();
		if (arkivJournalpost.dokumenter() == null) {
			return List.of();
		}
		return arkivJournalpost.dokumenter().stream()
				.map(arkivDokumentinfo -> {
					Long dokumentInfoId = arkivDokumentinfo.dokumentInfoId();
					return TilgangDokumentInfo.builder()
							.skjerming(mapSkjerming(arkivDokumentinfo.skjerming()))
							.tilgangDokumentvarianter(mapTilgangDokumentvarianter(journalpostId, dokumentInfoId, arkivDokumentinfo.fildetaljer()))
							.journalpostId(valueOf(journalpostId))
							.dokumentInfoId(valueOf(dokumentInfoId))
							.build();
				}).collect(Collectors.toList());
	}

	public static List<TilgangDokumentvariant> mapTilgangDokumentvarianter(Long journalpostId, Long dokumentInfoId, List<ArkivFildetaljer> fildetaljer) {
		return fildetaljer.stream()
				.map(arkivFildetaljer -> TilgangDokumentvariant.builder()
						.skjerming(mapSkjerming(arkivFildetaljer.skjerming()))
						.variantformat(VariantFormatCode.toSafVariantformat(arkivFildetaljer.format()))
						.journalpostId(valueOf(journalpostId))
						.dokumentInfoId(valueOf(dokumentInfoId))
						.build()).collect(Collectors.toList());
	}

	public static Skjerming mapSkjerming(String skjerming) {
		try {
			return skjerming == null ? null : SkjermingTypeCode.valueOf(skjerming).getSafSkjerming();
		} catch (IllegalArgumentException e) {
			// I tilfelle det introduseres en ny kodeverdi her uten at denne appen er i synk
			return Skjerming.FEIL;
		}
	}
}
