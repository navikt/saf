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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

	Set<TilgangBruker> tilgangBrukere(final List<JournalpostDto> tilknyttetJournalpostDto) {
		Set<Sakstilknytning> saker = tilknyttetJournalpostDto.stream()
				.map(journalpostDto -> {
					if (journalpostDto.isTilknyttetSak()) {
						SaksrelasjonDto saksrelasjon = journalpostDto.getSaksrelasjon();
						return new Sakstilknytning(saksrelasjon.getSakId(), mapJoarkFagsystemToArkivsakssystem(saksrelasjon.getFagsystem()));
					}
					return null;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());


		return tilknyttetJournalpostDto.stream()
				.map(journalpostDto -> {
					if (journalpostDto.isTilknyttetSak()) {
						// Sakstilknytning
						return sakstilknyttetTilgangBruker(journalpostDto.getSaksrelasjon());
					} else if (journalpostDto.isTilknyttetBruker()) {
						// Midlertidig
						BrukerDto bruker = journalpostDto.getBruker();
						return midlertidigTilgangBrukerPersonOrganisasjon(bruker);
					} else {
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
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

	private TilgangBruker sakstilknyttetTilgangBruker(SaksrelasjonDto saksrelasjon) {
		// For å finne den ekte brukeren på saken så må vi slå opp andre steder
		if (saksrelasjon.getFagsystem() == FagsystemCode.FS22) {
			// GSAK
			TilgangBruker tilgangBruker = gsakAntiCorruptionLayer.findTilgangBrukerBySakId(saksrelasjon.getSakId());
			return aktoerAntiCorruptionLayer.hentTilgangBrukerByAktoerId(tilgangBruker.getAktoerId());
		} else if (saksrelasjon.getFagsystem() == FagsystemCode.PEN) {
			// PSAK
			String foedselsnummer = pensjonSakAntiCorruptionLayer.findFoedselsnummerBySakId(saksrelasjon.getSakId());
			return aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(foedselsnummer);
		} else {
			return null;
		}
	}

	private TilgangBruker midlertidigTilgangBrukerPersonOrganisasjon(BrukerDto bruker) {
		if (bruker.isPerson()) {
			return TilgangBruker.builder()
					.foedselsnr(bruker.getBrukerId())
					.build();
		} else if (bruker.isOrganisasjon()) {
			return TilgangBruker.builder()
					.orgnummer(bruker.getBrukerId())
					.build();
		} else {
			return null;
		}
	}

	Set<TilgangSak> tilgangSaker(final Set<TilgangBruker> tilgangBrukere,
								 final List<JournalpostDto> datagrunnlag,
								 final SafRequestContext safRequestContext) {

		return new HashSet<>();
	}

	private TilgangSak tilgangSakGsak(SaksrelasjonDto saksrelasjon, SafRequestContext safRequestContext) {
		Arkivsak arkivsak = gsakAntiCorruptionLayer.findArkivsakBySakId(saksrelasjon.getSakId());
		safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
		final BidragSak bidragSak = getBidragSakIfTemaIsBidOrFar(arkivsak);
		return TilgangSak.builder()
				.aktoerId(arkivsak.getAktoerId())
				.orgnummer(arkivsak.getOrgnummer())
				.arkivsaksnummer(arkivsak.getArkivsaksnummer())
				.arkivsaksystem(arkivsak.getArkivsaksystem())
				.tema(arkivsak.getTema())
				.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
				.paragraf19(bidragSak == null ? null : bidragSak.isParagraf19())
				.build();
	}

	private TilgangSak tilgangSakPsak(TilgangBruker tilgangBruker, SaksrelasjonDto saksrelasjon, SafRequestContext safRequestContext) {
//		List<Arkivsak> arkivsaker = pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBrukerList.get(0), tema);
//		return arkivsaker.stream()
//				.map(arkivsak -> {
//					safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
//					return TilgangSak.builder()
//							.aktoerId(arkivsak.getAktoerId())
//							.arkivsaksnummer(arkivsak.getArkivsaksnummer())
//							.arkivsaksystem(arkivsak.getArkivsaksystem())
//							.tema(arkivsak.getTema())
//							.paragraf19(false)
//							.relevanteTredjeparter(new ArrayList<>())
//							.build();
//				}).collect(Collectors.toList());
		return null;
	}

	private BidragSak getBidragSakIfTemaIsBidOrFar(Arkivsak arkivsak) {
		if (Tema.BID.equals(arkivsak.getTema()) || Tema.FAR.equals(arkivsak.getTema())) {
			return bisysAntiCorruptionLayer.hentBidragSak(arkivsak.getFagsakId());
		} else {
			return new BidragSak();
		}
	}

	List<TilgangJournalpost> tilgangJournalposter(List<JournalpostDto> datagrunnlag) {
		return datagrunnlag.stream().map(this::mapTilgangJournalpost).collect(Collectors.toList());
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
