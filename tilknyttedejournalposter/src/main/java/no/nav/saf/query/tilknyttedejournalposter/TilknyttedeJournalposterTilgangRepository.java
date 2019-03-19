package no.nav.saf.query.tilknyttedejournalposter;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.FS22;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode.PEN;

import no.nav.saf.anticorruptionlayer.aktoer.AktoerAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.FagsystemCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.HentJournalsakinfo;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark903.BrukerDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark903.JournalpostDto;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark903.SaksrelasjonDto;
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
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
class TilknyttedeJournalposterTilgangRepository {
	private final HentJournalsakinfo hentJournalsakinfo;
	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;

	@Inject
	TilknyttedeJournalposterTilgangRepository(HentJournalsakinfo hentJournalsakinfo,
											  GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
											  PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
											  AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer,
											  BisysAntiCorruptionLayer bisysAntiCorruptionLayer) {
		this.hentJournalsakinfo = hentJournalsakinfo;
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
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

	Stream<TilgangBruker> sakstilknyttedeTilgangBrukere(final Set<Arkivsak> arkivsaker) {
		return arkivsaker.stream()
				.map(this::sakstilknyttetTilgangBruker);
	}

	Stream<TilgangBruker> ikkeSakstilknyttedeTilgangBrukere(final List<JournalpostDto> tilknyttetJournalpostDto) {
		return tilknyttetJournalpostDto.stream()
				.map(journalpostDto -> midlertidigTilgangBrukerPersonOrganisasjon(journalpostDto.getBruker()));
	}

	private Arkivsakssystem mapJoarkFagsystemToArkivsakssystem(FagsystemCode joarkFagsystem) {
		if (FS22 == joarkFagsystem) {
			return Arkivsakssystem.GSAK;
		} else if (PEN == joarkFagsystem) {
			return Arkivsakssystem.PSAK;
		} else {
			return null;
		}
	}

	private TilgangBruker sakstilknyttetTilgangBruker(Arkivsak arkivsak) {
		// For å finne den ekte brukeren på saken så må vi slå opp andre steder
		if (arkivsak.getArkivsaksystem() == Arkivsakssystem.GSAK) {
			// GSAK
			TilgangBruker tilgangBruker = gsakAntiCorruptionLayer.findTilgangBrukerBySakId(arkivsak.getArkivsaksnummer());
			return aktoerAntiCorruptionLayer.hentTilgangBrukerByAktoerId(tilgangBruker.getAktoerId());
		} else if (arkivsak.getArkivsaksystem() == Arkivsakssystem.PSAK) {
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

	Set<TilgangSak> tilgangSaker(Set<TilgangBruker> filteredTilgangBruker, final Set<Arkivsak> arkivsaker,
								 final SafRequestContext safRequestContext) {
		if (filteredTilgangBruker.isEmpty() || arkivsaker.isEmpty()) {
			return new HashSet<>();
		}

		return arkivsaker.stream()
				.map(arkivsak -> {
					if (arkivsak.getArkivsaksystem() == Arkivsakssystem.GSAK) {
						return tilgangSakGsak(arkivsak, safRequestContext);
					} else if (arkivsak.getArkivsaksystem() == Arkivsakssystem.PSAK) {
						return tilgangSakPsak(arkivsak, safRequestContext);
					} else {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
	}

	private TilgangSak tilgangSakGsak(final Arkivsak arkivsak, SafRequestContext safRequestContext) {
		Arkivsak gsakArkivsak = gsakAntiCorruptionLayer.findArkivsakBySakId(arkivsak.getArkivsaksnummer());
		safRequestContext.getRequestCache().putObject(gsakArkivsak.getKey(), gsakArkivsak);
		final BidragSak bidragSak = getBidragSakIfTemaIsBidOrFar(gsakArkivsak);
		return TilgangSak.builder()
				.aktoerId(gsakArkivsak.getAktoerId())
				.orgnummer(gsakArkivsak.getOrgnummer())
				.arkivsaksnummer(gsakArkivsak.getArkivsaksnummer())
				.arkivsaksystem(gsakArkivsak.getArkivsaksystem())
				.tema(gsakArkivsak.getTema())
				.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
				.paragraf19(bidragSak == null ? null : bidragSak.isParagraf19())
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
							.tema(psakArkivsak.getTema())
							.paragraf19(false)
							.relevanteTredjeparter(new ArrayList<>())
							.build();
				}).findFirst().orElse(null);
	}

	private BidragSak getBidragSakIfTemaIsBidOrFar(Arkivsak arkivsak) {
		if (Tema.BID.equals(arkivsak.getTema()) || Tema.FAR.equals(arkivsak.getTema())) {
			return bisysAntiCorruptionLayer.hentBidragSak(arkivsak.getFagsakId());
		} else {
			return new BidragSak();
		}
	}

	List<TilgangJournalpost> tilgangJournalposter(Set<TilgangSak> filteredTilgangSaker, List<JournalpostDto> datagrunnlag) {
		return datagrunnlag.stream()
				.filter(journalpostDto -> {
					if(journalpostDto.isTilknyttetSak()) {
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
