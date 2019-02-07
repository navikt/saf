package no.nav.saf.domain;

import static no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayerImpl.PSAK_FAGSYSTEM;
import static no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayerImpl.TEMA_PENSJON;
import static no.nav.saf.domain.DomainConstants.AKTOER_ID_LIST;
import static no.nav.saf.domain.DomainConstants.ORGNR_LIST;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.AktoerAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.SkjermingTypeCode;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;
import no.nav.saf.tjeneste.argumenter.FagsakInput;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
@Slf4j
public class TilgangsmodellRepositoryImpl implements TilgangsmodellRepository {
	public static final int MAX_ARKIVSAKER_LOGG = 1000;

	private final AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer;
	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final JoarkAntiCorruptionLayer joarkAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;

	@Inject
	public TilgangsmodellRepositoryImpl(AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer,
										GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
										PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
										JoarkAntiCorruptionLayer joarkAntiCorruptionLayer,
										BisysAntiCorruptionLayer bisysAntiCorruptionLayer) {
		this.aktoerAntiCorruptionLayer = aktoerAntiCorruptionLayer;
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.joarkAntiCorruptionLayer = joarkAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
	}

	@Override
	@Cacheable(cacheNames = LokalCacheConfig.TILGANGSMODELL_REPO_BRUKER_CACHE)
	public TilgangBruker findTilgangBruker(BrukerIdInput brukerIdInput) {
		try {
			switch (brukerIdInput.getType()) {
				case AKTOERID:
					return aktoerAntiCorruptionLayer.hentTilgangBrukerByAktoerId(brukerIdInput.getId());
				case FNR:
					return aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(brukerIdInput.getId());
				case ORGNR:
					return TilgangBruker.builder()
							.orgnummer(brukerIdInput.getId())
							.build();
				default:
					return null;
			}
		} catch (Exception e) {
			log.warn("findTilgangBruker feilet ved oppslag av id. type={}", brukerIdInput.getType(), e);
		}
		return null;
	}

	@Override
	@Cacheable(cacheNames = LokalCacheConfig.TILGANGSMODELL_REPO_BRUKER_CACHE)
	public List<TilgangBruker> findTilgangBrukerList(FagsakInput fagsakInput) {
		try {
			if (fagsakInput.getFagsaksystem().equals(PSAK_FAGSYSTEM)) {
				return findTilgangBrukerListForPensjonsakerByFagsakId(fagsakInput);
			} else {
				return findTilgangBrukerListForGsaksakerByFagsakIdAndFagsaksystem(fagsakInput);
			}
		} catch (Exception e) {
			log.warn("findTilgangBrukerList feilet ved oppslag. fagsakInput={}", fagsakInput, e);
		}
		return new ArrayList<>();
	}


	private List<TilgangBruker> findTilgangBrukerListForGsaksakerByFagsakIdAndFagsaksystem(FagsakInput fagsakInput) {
		try {
			Map<String, List<String>> idLists = gsakAntiCorruptionLayer.findIdListsByFagsakIdAndFagsaksystem(fagsakInput.getFagsakId(), fagsakInput
					.getFagsaksystem());
			if (idLists.isEmpty()) {
				return new ArrayList<>();
			}

			List<TilgangBruker> tilgangBrukerPerson = aktoerAntiCorruptionLayer.hentTilgangBrukerListByAktoerIdList(idLists.get(AKTOER_ID_LIST));
			List<TilgangBruker> tilgangbrukerOrganisasjon = idLists.get(ORGNR_LIST).stream()
					.map(orgnr -> TilgangBruker.builder().orgnummer(orgnr).build())
					.collect(Collectors.toList());

			return Stream.concat(tilgangBrukerPerson.stream(), tilgangbrukerOrganisasjon.stream()).collect(Collectors.toList());
		} catch (Exception e) {
			log.warn("findTilgangBrukerListForGsaksakerByFagsakIdAndFagsaksystem feilet ved oppslag. fagsakInput={}", fagsakInput, e);
			return new ArrayList<>();
		}
	}

	private List<TilgangBruker> findTilgangBrukerListForPensjonsakerByFagsakId(FagsakInput fagsakInput) {
		try {
			String fnr = pensjonSakAntiCorruptionLayer.findFoedselsnummerBySakId(fagsakInput.getFagsakId());
			return Arrays.asList(aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(fnr));
		} catch (Exception e) {
			log.warn("findTilgangBrukerListForPensjonsakerByFagsakId feilet ved oppslag. fagsakInput={}", fagsakInput, e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<TilgangSak> findTilgangSaker(final List<TilgangBruker> tilgangBrukerList, final FagsakInput fagsakInput, final List<Tema> tema, final SafRequestContext safRequestContext) {
		if (fagsakInput.getFagsaksystem().equals(PSAK_FAGSYSTEM)) {
			return findTilgangSakForPsaker(tilgangBrukerList, fagsakInput, tema, safRequestContext);
		} else {
			return findTilgangSakForGsaker(tilgangBrukerList, fagsakInput, tema, safRequestContext);

		}
	}

	private List<TilgangSak> findTilgangSakForGsaker(List<TilgangBruker> filteredTilgangBrukerList, FagsakInput fagsakInput, List<Tema> tema, SafRequestContext safRequestContext) {
		try {
//			For å unngå å gjøre ett kall for hver bruker hentes alle saker assosiert med det aktuelle fagsaknummeret og fagsaksystemet fra Gsak i én spørring
			List<Arkivsak> arkivsaker = gsakAntiCorruptionLayer.findTilgangSakListByFagsakIdAndFagsaksystem(fagsakInput.getFagsakId(), fagsakInput
					.getFagsaksystem(), tema);

			List<String> aktoerIdList = extractAktoerIdListFromFilteredTilgangBrukerList(filteredTilgangBrukerList);
			List<String> orgnrList = extractOrgnrListFromFilteredTilgangBrukerList(filteredTilgangBrukerList);

			return arkivsaker.stream()
//					Vi er kun interesserte i saker tilhørende brukere som ikke ble filtrert bort i pep1
					.filter(arkivsak -> aktoerIdList.contains(arkivsak.getAktoerId()) || orgnrList.contains(arkivsak.getOrgnummer()))
					.map(arkivsak -> {
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
					}).collect(Collectors.toList());
		} catch (Exception e) {
			log.warn("findTilgangSakForGsaker feilet ved for fagsakInput={}.", fagsakInput, e);
		}
		return new ArrayList<>();
	}

	private List<String> extractAktoerIdListFromFilteredTilgangBrukerList(List<TilgangBruker> filteredTilgangBrukerList) {
		return filteredTilgangBrukerList.stream()
				.filter(TilgangBruker::isBrukerPerson)
				.map(TilgangBruker::getAktoerId)
				.collect(Collectors.toList());
	}

	private List<String> extractOrgnrListFromFilteredTilgangBrukerList(List<TilgangBruker> filteredTilgangBrukerList) {
		return filteredTilgangBrukerList.stream()
				.filter(tilgangBruker -> !tilgangBruker.isBrukerPerson())
				.map(TilgangBruker::getOrgnummer)
				.collect(Collectors.toList());
	}

	private List<TilgangSak> findTilgangSakForPsaker(List<TilgangBruker> tilgangBrukerList, FagsakInput fagsakInput, List<Tema> tema, SafRequestContext safRequestContext) {
		try {
			if (tilgangBrukerList.size() != 1) {
				log.warn("findTilgangSakForPsaker ble kalt med null eller mer enn én bruker. Pensjonssaker kan kun ha én bruker.");
				return new ArrayList<>();
			}

			List<Arkivsak> arkivsaker = pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBrukerList.get(0), tema);
			return arkivsaker.stream()
					.map(arkivsak -> {
						safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
						return TilgangSak.builder()
								.aktoerId(arkivsak.getAktoerId())
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(arkivsak.getArkivsaksystem())
								.tema(arkivsak.getTema())
								.paragraf19(false)
								.relevanteTredjeparter(new ArrayList<>())
								.build();
					}).collect(Collectors.toList());
		} catch (Exception e) {
			log.warn("findTilgangSakForPsaker feilet ved for fagsakInput={}.", fagsakInput, e);
		}
		return new ArrayList<>();
	}

	@Override
	public Flowable<TilgangSak> findTilgangSaker(final TilgangBruker tilgangBruker, final List<Tema> tema, final SafRequestContext safRequestContext) {
		try {
			Flowable<List<Arkivsak>> gsakerFromOrgnr = Flowable.fromCallable(() ->
					gsakAntiCorruptionLayer.findArkivsakerByOrgnr(tilgangBruker.getOrgnummer(), tema))
					.subscribeOn(Schedulers.io());
			Flowable<List<Arkivsak>> gsakerFromAktoerId = Flowable.fromCallable(() ->
					gsakAntiCorruptionLayer.findArkivsakerByAktoerId(tilgangBruker.getAktoerId(), tema))
					.subscribeOn(Schedulers.io());
			Flowable<List<Arkivsak>> psaker = Flowable.fromCallable(() -> {
				if (!Collections.disjoint(tema, TEMA_PENSJON)) {
					return pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker, tema);
				} else {
					return new ArrayList<Arkivsak>();
				}
			}).subscribeOn(Schedulers.io());
			return Flowable.merge(Arrays.asList(gsakerFromOrgnr, gsakerFromAktoerId, psaker), 3)
					.flatMapIterable(items -> items)
					.map(arkivsak -> {
						final BidragSak bidragSak = getBidragSakIfTemaIsBidOrFar(arkivsak);
						safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
						return TilgangSak.builder()
								.aktoerId(arkivsak.getAktoerId())
								.orgnummer(arkivsak.getOrgnummer())
								.tema(arkivsak.getTema())
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(arkivsak.getArkivsaksystem())
								.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
								.paragraf19(bidragSak == null ? null : bidragSak.isParagraf19())
								.build();
					});
		} catch (Exception e) {
			return Flowable.empty();
		}
	}

	private BidragSak getBidragSakIfTemaIsBidOrFar(Arkivsak arkivsak) {
		if (Tema.BID.equals(arkivsak.getTema()) || Tema.FAR.equals(arkivsak.getTema())) {
			return bisysAntiCorruptionLayer.hentBidragSak(arkivsak.getArkivsaksnummer());
		} else {
			return new BidragSak();
		}
	}

	@Override
	public List<TilgangJournalpost> findTilgangJournalposter(List<TilgangBruker> tilgangBrukere,
															 List<TilgangSak> tilgangSakList,
															 LocalDate fraDato,
															 List<Tema> inkluderTema, List<Journalposttype> inkluderJournalposttyper,
															 List<Journalstatus> inkluderJournalstatuses,
															 Integer foerste, String etterPeker, Integer siste, String foerPeker,
															 SafRequestContext safRequestContext) {
		try {
			List<String> identer = tilgangBrukere.stream()
					.flatMap(t -> t.getAlleIdenter().stream())
					.collect(Collectors.toList());
			List<JournalpostDto> journalposter = joarkAntiCorruptionLayer.finnJournalposter(identer,
					tilgangSakList, fraDato, inkluderTema, inkluderJournalposttyper, inkluderJournalstatuses, foerste, etterPeker, siste, foerPeker);
			return journalposter.stream()
					.map(journalpostDto -> {
						safRequestContext.getRequestCache()
								.putObject(journalpostDto.getJournalpostId().toString(), journalpostDto);
						return mapTilgangJournalpost(journalpostDto);
					})
					.collect(Collectors.toList());
		} catch (Exception e) {
			if (tilgangSakList.size() < MAX_ARKIVSAKER_LOGG) {
				List<String> arkivsaksId = tilgangSakList.stream()
						.map(TilgangSak::getArkivsaksnummer)
						.collect(Collectors.toList());
				log.warn("finnJournalposter feilet ved henting av journalposter på arkivsaker={}.", arkivsaksId, e);
			} else {
				log.warn("finnJournalposter feilet ved henting av journalposter på arkivsaker. Det var flere enn 1000 arkivsaker. Disse logges ikke da så lange logglinjer ikke støttes i logstash.", e);
			}
			return new ArrayList<>();
		}
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
