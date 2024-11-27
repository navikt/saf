package no.nav.saf.query.tilknyttedejournalposter;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.PdlAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivBruker;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSak;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSaksrelasjon;
import no.nav.saf.anticorruptionlayer.pdl.PersonIkkeFunnetException;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.kode.Skjerming;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.IdentType;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.saf.domain.kode.Arkivsakssystem.GSAK;
import static no.nav.saf.domain.kode.Arkivsakssystem.PSAK;
import static no.nav.saf.domain.tilgangsmodell.IdentType.AKTOERID;
import static no.nav.saf.domain.tilgangsmodell.IdentType.FOLKEREGISTERIDENT;

@Slf4j
@Component
public class TilknyttedeJournalposterTilgangRepository {

	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final PdlAntiCorruptionLayer aktoerAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;

	TilknyttedeJournalposterTilgangRepository(PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
											  PdlAntiCorruptionLayer aktoerAntiCorruptionLayer,
											  BisysAntiCorruptionLayer bisysAntiCorruptionLayer) {
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.aktoerAntiCorruptionLayer = aktoerAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
	}

	Set<Arkivsak> arkivsaker(List<ArkivJournalpost> tilknyttetArkivJournalposter, SafRequestContext safRequestContext) {
		return tilknyttetArkivJournalposter.stream()
				.filter(ArkivJournalpost::isTilknyttetSak)
				.map(arkivJournalpost -> {
					ArkivSaksrelasjon saksrelasjon = arkivJournalpost.saksrelasjon();
					ArkivSak sak = saksrelasjon.sak();
					Arkivsak.ArkivsakBuilder arkivsakBuilder = Arkivsak.builder()
							.arkivsaksnummer(String.valueOf(saksrelasjon.sakId()))
							.arkivsaksystem(FagsystemCode.toSafArkivsaksystem(saksrelasjon.fagsystem()));

					if (sak != null) {
						arkivsakBuilder.fagsaksystem(sak.applikasjon())
								.fagsakId(sak.fagsakNr())
								.orgnummer(sak.orgNr())
								.aktoerId(sak.aktoerId())
								.tema(Arkivsak.mapTema(saksrelasjon.sak().tema()))
								.datoOpprettet(sak.opprettetTid());
					}
					return arkivsakBuilder.build();
				})
				.collect(Collectors.toSet());
	}

	Set<TilgangBruker> tilgangBrukere(final Set<Arkivsak> arkivsaker, final List<ArkivJournalpost> tilknyttetJournalpostDto) {
		return Stream.concat(sakstilknyttedeTilgangBrukere(arkivsaker), ikkeSakstilknyttedeTilgangBrukere(tilknyttetJournalpostDto)).collect(Collectors.toSet());
	}

	private Stream<TilgangBruker> sakstilknyttedeTilgangBrukere(final Set<Arkivsak> arkivsaker) {
		return arkivsaker.stream()
				.map(this::sakstilknyttetTilgangBruker);
	}

	private Stream<TilgangBruker> ikkeSakstilknyttedeTilgangBrukere(final List<ArkivJournalpost> tilknyttetJournalpostDto) {
		return tilknyttetJournalpostDto.stream()
				.filter(arkivJournalpost -> !arkivJournalpost.isTilknyttetSak())
				.map(journalpost -> midlertidigTilgangBrukerPersonOrganisasjon(journalpost.bruker()));
	}

	private TilgangBruker sakstilknyttetTilgangBruker(Arkivsak arkivsak) {
		// For å finne den ekte brukeren på saken så må vi slå opp andre steder
		if (arkivsak.getArkivsaksystem() == GSAK) {
			// GSAK
			TilgangBruker tilgangBruker = TilgangBruker.builder()
					.aktoerId(arkivsak.getAktoerId())
					.orgnummer(arkivsak.getAktoerId() == null ? arkivsak.getOrgnummer() : null)
					.build();
			if (!tilgangBruker.isPerson()) {
				return tilgangBruker;
			} else {
				return hentTilgangBruker(arkivsak.getAktoerId(), AKTOERID);
			}
		} else if (arkivsak.getArkivsaksystem() == PSAK) {
			// PSAK
			String foedselsnummer = pensjonSakAntiCorruptionLayer.findFoedselsnummerBySakId(arkivsak.getArkivsaksnummer());
			return hentTilgangBruker(foedselsnummer, FOLKEREGISTERIDENT);
		} else {
			return null;
		}
	}

	private TilgangBruker midlertidigTilgangBrukerPersonOrganisasjon(ArkivBruker bruker) {
		if (bruker == null) {
			return TilgangBruker.builder().build();
		} else if (bruker.isPerson()) {
			return hentTilgangBruker(bruker.id(), FOLKEREGISTERIDENT);
		} else if (bruker.isOrganisasjon()) {
			return TilgangBruker.builder()
					.orgnummer(bruker.id())
					.build();
		} else {
			return TilgangBruker.builder().build();
		}
	}

	private TilgangBruker hentTilgangBruker(String brukerId, IdentType identType) {
		try {
			if (identType == FOLKEREGISTERIDENT) {
				return aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(brukerId);
			}
			return aktoerAntiCorruptionLayer.hentTilgangBrukerByAktoerId(brukerId);
		} catch (PersonIkkeFunnetException e) {
			log.info("Fant ikke person i Persondataløsningen (PDL).");
			return TilgangBruker.builder().build();
		}
	}

	Set<TilgangSak> tilgangSaker(final Set<Arkivsak> arkivsaker,
								 final SafRequestContext safRequestContext) {
		return arkivsaker.stream()
				.map(arkivsak -> {
					if (arkivsak.getArkivsaksystem() == GSAK) {
						return tilgangSakGsak(arkivsak, safRequestContext);
					} else if (arkivsak.getArkivsaksystem() == PSAK) {
						return tilgangSakPsak(arkivsak, safRequestContext);
					} else {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private TilgangSak tilgangSakGsak(final Arkivsak arkivsak, final SafRequestContext safRequestContext) {
		final BidragSak bidragSak = bisysAntiCorruptionLayer.hentBidragSakByArkivsak(arkivsak);
		safRequestContext.getRequestCache().putArkivsak(arkivsak);
		return TilgangSak.builder()
				.aktoerId(arkivsak.getAktoerId())
				.orgnummer(arkivsak.getOrgnummer())
				.arkivsaksnummer(arkivsak.getArkivsaksnummer())
				.arkivsaksystem(arkivsak.getArkivsaksystem())
				.fagsaksystem(arkivsak.getFagsaksystem())
				.tema(arkivsak.getTema())
				.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
				.build();
	}

	private TilgangSak tilgangSakPsak(Arkivsak arkivsak, SafRequestContext safRequestContext) {
		String foedselsnummer = pensjonSakAntiCorruptionLayer.findFoedselsnummerBySakId(arkivsak.getArkivsaksnummer());
		TilgangBruker tilgangBruker = aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(foedselsnummer);
		List<Arkivsak> arkivsaker = pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker, Arrays.asList(Tema.PEN, Tema.UFO));
		return arkivsaker.stream()
				.filter(psakArkivsak -> psakArkivsak.getArkivsaksnummer().equals(arkivsak.getArkivsaksnummer()))
				.map(psakArkivsak -> {
					safRequestContext.getRequestCache().putArkivsak(psakArkivsak);
					return TilgangSak.builder()
							.aktoerId(psakArkivsak.getAktoerId())
							.arkivsaksnummer(psakArkivsak.getArkivsaksnummer())
							.arkivsaksystem(psakArkivsak.getArkivsaksystem())
							.fagsaksystem(psakArkivsak.getFagsaksystem())
							.tema(psakArkivsak.getTema())
							.relevanteTredjeparter(new ArrayList<>())
							.build();
				}).findFirst().orElse(null);
	}

	List<TilgangJournalpost> tilgangJournalposter(Set<TilgangSak> filteredTilgangSaker, List<ArkivJournalpost> datagrunnlag) {
		return datagrunnlag.stream()
				.filter(arkivJournalpost -> {
					if (arkivJournalpost.isTilknyttetSak()) {
						return filteredTilgangSaker.stream().anyMatch(tilgangSak -> {
							ArkivSaksrelasjon saksrelasjon = arkivJournalpost.saksrelasjon();
							return tilgangSak.getArkivsaksnummer().equals(saksrelasjon.sakId().toString()) && tilgangSak.getArkivsaksystem() == FagsystemCode.toSafArkivsaksystem(saksrelasjon.fagsystem());
						});
					} else {
						return true;
					}
				})
				.map(this::mapTilgangJournalpost)
				.toList();
	}

	private TilgangJournalpost mapTilgangJournalpost(ArkivJournalpost arkivJournalpost) {
		return TilgangJournalpost.builder()
				.journalpostId(arkivJournalpost.journalpostId().toString())
				.journalstatus(JournalStatusCode.valueOf(arkivJournalpost.status()).toSafJournalstatus())
				.skjerming(arkivJournalpost.skjerming() != null ? Skjerming.valueOf(arkivJournalpost.skjerming()) : null)
				.dokumenter(arkivJournalpost.dokumenter().stream().map(dokdto ->
						TilgangDokumentInfo.builder()
								.journalpostId(arkivJournalpost.journalpostId().toString())
								.dokumentInfoId(dokdto.dokumentInfoId().toString())
								.skjerming(dokdto.skjerming() != null ? Skjerming.valueOf(dokdto.skjerming()) : null)
								.tilgangDokumentvarianter(dokdto.fildetaljer().stream()
										.map(variantDto -> TilgangDokumentvariant.builder()
												.skjerming(variantDto.skjerming() != null ? Skjerming.valueOf(variantDto.skjerming()) : null)
												.variantformat(VariantFormatCode.toSafVariantformat(variantDto.format()))
												.journalpostId(arkivJournalpost.journalpostId().toString())
												.dokumentInfoId(dokdto.dokumentInfoId().toString())
												.build())
										.toList()
								)
								.build()).toList())
				.build();
	}
}
