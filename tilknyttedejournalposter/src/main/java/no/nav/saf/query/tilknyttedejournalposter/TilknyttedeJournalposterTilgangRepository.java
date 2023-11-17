package no.nav.saf.query.tilknyttedejournalposter;

import no.nav.saf.anticorruptionlayer.aktoer.PdlAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.BrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.dto.SaksrelasjonDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark903.TilknytningUriParam;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.kode.Arkivsakssystem;
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.FS22;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.PEN;
import static no.nav.saf.domain.DomainConstants.TIDSSONE_NORGE;
import static no.nav.saf.domain.kode.Arkivsakssystem.GSAK;
import static no.nav.saf.domain.kode.Arkivsakssystem.PSAK;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
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

	Set<Arkivsak> arkivsaker(final List<JournalpostDto> tilknyttetJournalpostDto, SafRequestContext safRequestContext) {
		return tilknyttetJournalpostDto.stream()
				.map(journalpostDto -> {
					safRequestContext.getRequestCache().putObject(journalpostDto.getJournalpostId().toString(), journalpostDto);
					if (journalpostDto.isTilknyttetSak()) {
						SaksrelasjonDto saksrelasjon = journalpostDto.getSaksrelasjon();
						return Arkivsak.builder()
								.arkivsaksnummer(saksrelasjon.getSakId())
								.arkivsaksystem(mapJoarkFagsystemToArkivsakssystem(saksrelasjon.getFagsystem()))
								.fagsaksystem(saksrelasjon.getApplikasjon())
								.fagsakId(saksrelasjon.getFagsakNr())
								.orgnummer(saksrelasjon.getOrgnr())
								.aktoerId(saksrelasjon.getAktoerId())
								.tema(Arkivsak.mapTema(saksrelasjon.getTema()))
								.datoOpprettet(Optional.ofNullable(saksrelasjon.getOpprettetTidspunkt())
										.map(o -> o.atZoneSameInstant(TIDSSONE_NORGE))
										.map(ZonedDateTime::toLocalDateTime)
										.orElse(null))
								.build();
					}
					return null;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	Set<TilgangBruker> tilgangBrukere(final Set<Arkivsak> arkivsaker, final List<JournalpostDto> tilknyttetJournalpostDto) {
		return Stream.concat(sakstilknyttedeTilgangBrukere(arkivsaker), ikkeSakstilknyttedeTilgangBrukere(tilknyttetJournalpostDto)).collect(Collectors.toSet());
	}

	private Stream<TilgangBruker> sakstilknyttedeTilgangBrukere(final Set<Arkivsak> arkivsaker) {
		return arkivsaker.stream()
				.map(this::sakstilknyttetTilgangBruker);
	}

	private Stream<TilgangBruker> ikkeSakstilknyttedeTilgangBrukere(final List<JournalpostDto> tilknyttetJournalpostDto) {
		return tilknyttetJournalpostDto.stream()
				.map(journalpostDto -> {
					if (journalpostDto.isTilknyttetSak()) {
						return null;
					} else {
						return midlertidigTilgangBrukerPersonOrganisasjon(journalpostDto.getBruker());
					}
				}).filter(Objects::nonNull);
	}

	private Arkivsakssystem mapJoarkFagsystemToArkivsakssystem(FagsystemCode joarkFagsystem) {
		if (FS22 == joarkFagsystem) {
			return GSAK;
		} else if (PEN == joarkFagsystem) {
			return PSAK;
		} else {
			return null;
		}
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

	private TilgangBruker midlertidigTilgangBrukerPersonOrganisasjon(BrukerDto bruker) {
		if (bruker.isPerson()) {
			return aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(bruker.getBrukerId());
		} else if (bruker.isOrganisasjon()) {
			return TilgangBruker.builder()
					.orgnummer(bruker.getBrukerId())
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
		safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
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
					safRequestContext.getRequestCache().putObject(psakArkivsak.getKey(), psakArkivsak);
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

	List<TilgangJournalpost> tilgangJournalposter(Set<TilgangSak> filteredTilgangSaker, List<JournalpostDto> datagrunnlag) {
		return datagrunnlag.stream()
				.filter(journalpostDto -> {
					if (journalpostDto.isTilknyttetSak()) {
						return filteredTilgangSaker.stream().anyMatch(tilgangSak -> {
							SaksrelasjonDto saksrelasjon = journalpostDto.getSaksrelasjon();
							return tilgangSak.getArkivsaksnummer().equals(saksrelasjon.getSakId()) &&
									tilgangSak.getArkivsaksystem() == mapJoarkFagsystemToArkivsakssystem(saksrelasjon.getFagsystem());
						});
					} else {
						return true;
					}
				})
				.map(this::mapTilgangJournalpost)
				.collect(Collectors.toList());
	}

	private TilgangJournalpost mapTilgangJournalpost(JournalpostDto dto) {
		return TilgangJournalpost.builder()
				.journalpostId(dto.getJournalpostId().toString())
				.journalstatus(dto.getJournalstatus().toSafJournalstatus())
				.skjerming(SkjermingTypeCode.toSafSkjerming(dto.getSkjerming()))
				.dokumenter(dto.getDokumenter().stream().map(dokdto -> TilgangDokumentInfo.builder()
						.journalpostId(dto.getJournalpostId().toString())
						.dokumentInfoId(dokdto.getDokumentInfoId())
						.skjerming(SkjermingTypeCode.toSafSkjerming(dokdto.getSkjerming()))
						.tilgangDokumentvarianter(dokdto.getVarianter().stream()
								.map(variantDto -> TilgangDokumentvariant.builder()
										.skjerming(SkjermingTypeCode.toSafSkjerming(variantDto.getSkjerming()))
										.variantformat(VariantFormatCode.toSafVariantformat(variantDto.getVariantf()))
										.journalpostId(dto.getJournalpostId().toString())
										.dokumentInfoId(dokdto.getDokumentInfoId())
										.build())
								.collect(Collectors.toList())
						)
						.build()).collect(Collectors.toList()))
				.build();
	}
}
