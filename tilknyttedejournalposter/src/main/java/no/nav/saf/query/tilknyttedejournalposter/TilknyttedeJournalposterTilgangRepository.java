package no.nav.saf.query.tilknyttedejournalposter;

import no.nav.saf.anticorruptionlayer.aktoer.PdlAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark903.TilknytningUriParam;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivBruker;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivJournalpost;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSak;
import no.nav.saf.anticorruptionlayer.joark.safintern.journalpost.ArkivSaksrelasjon;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.kode.Skjerming;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.kode.Tilknytning;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
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

@Component
class TilknyttedeJournalposterTilgangRepository {

	private final HentJournalsakinfo hentJournalsakinfo;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final PdlAntiCorruptionLayer aktoerAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;

	@Autowired
	TilknyttedeJournalposterTilgangRepository(HentJournalsakinfo hentJournalsakinfo,
											  PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
											  PdlAntiCorruptionLayer aktoerAntiCorruptionLayer,
											  BisysAntiCorruptionLayer bisysAntiCorruptionLayer) {
		this.hentJournalsakinfo = hentJournalsakinfo;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.aktoerAntiCorruptionLayer = aktoerAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
	}

	List<JournalpostDto> datagrunnlag(final String dokumentInfoId, final Tilknytning tilknytning) {
		return hentJournalsakinfo.tilknyttedeJournalposter(dokumentInfoId, TilknytningUriParam.toUriParam(tilknytning))
				.getTilknyttedeJournalposter();
	}

	Set<Arkivsak> arkivsaker(final List<ArkivJournalpost> tilknyttetArkivJournalposter, SafRequestContext safRequestContext) {
		tilknyttetArkivJournalposter.forEach(arkivJournalpost ->
				safRequestContext.getRequestCache().putArkivJournalpost(arkivJournalpost.journalpostId().toString(), arkivJournalpost));

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
				.map(journalpost -> {
					if (journalpost.isTilknyttetSak()) {
						return null;
					} else {
						return midlertidigTilgangBrukerPersonOrganisasjon(journalpost.bruker());
					}
				}).filter(Objects::nonNull);
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
				return aktoerAntiCorruptionLayer.hentTilgangBrukerByAktoerId(tilgangBruker.getAktoerId());
			}
		} else if (arkivsak.getArkivsaksystem() == PSAK) {
			// PSAK
			String foedselsnummer = pensjonSakAntiCorruptionLayer.findFoedselsnummerBySakId(arkivsak.getArkivsaksnummer());
			return aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(foedselsnummer);
		} else {
			return null;
		}
	}

	private TilgangBruker midlertidigTilgangBrukerPersonOrganisasjon(ArkivBruker bruker) {
		if (bruker.isPerson()) {
			return aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(bruker.id());
		} else if (bruker.isOrganisasjon()) {
			return TilgangBruker.builder()
					.orgnummer(bruker.id())
					.build();
		} else {
			return null;
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
				.collect(Collectors.toList());
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
										.collect(Collectors.toList())
								)
								.build()).collect(Collectors.toList()))
				.build();
	}
}
