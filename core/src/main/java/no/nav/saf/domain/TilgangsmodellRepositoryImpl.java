package no.nav.saf.domain;

import static no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayerImpl.PSAK_FAGSYSTEM;
import static no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayerImpl.TEMA_PENSJON;
import static no.nav.saf.domain.DomainConstants.AKTOER_ID_LIST;
import static no.nav.saf.domain.DomainConstants.ORGNR_LIST;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.AktoerAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.hentjournalsakinfo.rjoark900.JournalpostDto;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;
import no.nav.saf.tjeneste.argumenter.FagsakIdInput;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalposttype;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalstatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
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

	@Inject
	public TilgangsmodellRepositoryImpl(AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer,
										GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
										PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
										JoarkAntiCorruptionLayer joarkAntiCorruptionLayer) {
		this.aktoerAntiCorruptionLayer = aktoerAntiCorruptionLayer;
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.joarkAntiCorruptionLayer = joarkAntiCorruptionLayer;
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
	public List<TilgangBruker> findTilgangBrukerList(FagsakIdInput fagsakIdInput) {
		try {
			if (fagsakIdInput.getFagsaksystem().equals(PSAK_FAGSYSTEM)) {
				return findTilgangBrukerListForPensjonsakerByFagsakId(fagsakIdInput);
			} else {
				return findTilgangBrukerListForGsaksakerByFagsakIdAndFagsaksystem(fagsakIdInput);
			}
		} catch (Exception e) {
			log.warn("findTilgangBrukerList feilet ved oppslag. fagsakIdInput={}", fagsakIdInput, e);
		}
		return new ArrayList<>();
	}


	private List<TilgangBruker> findTilgangBrukerListForGsaksakerByFagsakIdAndFagsaksystem(FagsakIdInput fagsakIdInput) {
		try {
			Map<String, List<String>> idLists = gsakAntiCorruptionLayer.findIdListsByFagsakIdAndFagsaksystem(fagsakIdInput.getFagsaksnummer(), fagsakIdInput
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
			log.warn("findTilgangBrukerListForGsaksakerByFagsakIdAndFagsaksystem feilet ved oppslag. fagsakIdInput={}", fagsakIdInput, e);
			return new ArrayList<>();
		}
	}

	private List<TilgangBruker> findTilgangBrukerListForPensjonsakerByFagsakId(FagsakIdInput fagsakIdInput) {
		try {
			String fnr = pensjonSakAntiCorruptionLayer.findFoedselsnummerBySakId(fagsakIdInput.getFagsaksnummer());
			return Arrays.asList(aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(fnr));
		} catch (Exception e) {
			log.warn("findTilgangBrukerListForPensjonsakerByFagsakId feilet ved oppslag. fagsakIdInput={}", fagsakIdInput, e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<TilgangSak> findTilgangSaker(final List<TilgangBruker> tilgangBrukerList, final FagsakIdInput fagsakIdInput, final List<Tema> tema, final SafRequestContext safRequestContext) {
		if (fagsakIdInput.getFagsaksystem().equals(PSAK_FAGSYSTEM)) {
			return findTilgangSakForPsaker(tilgangBrukerList, fagsakIdInput, tema, safRequestContext);
		} else {
			return findTilgangSakForGsaker(tilgangBrukerList, fagsakIdInput, tema, safRequestContext);

		}
	}

	private List<TilgangSak> findTilgangSakForGsaker(List<TilgangBruker> tilgangBrukerList, FagsakIdInput fagsakIdInput, List<Tema> tema, SafRequestContext safRequestContext) {
		try {
			List<Arkivsak> arkivsaker = gsakAntiCorruptionLayer.findTilgangSakListByFagsakIdAndFagsaksystem(fagsakIdInput.getFagsaksnummer(), fagsakIdInput
					.getFagsaksystem(), tema);
			return arkivsaker.stream()
					.filter(arkivsak -> {
						List<String> aktoerIdList = tilgangBrukerList.stream()
								.filter(TilgangBruker::isBrukerPerson)
								.map(TilgangBruker::getAktoerId)
								.collect(Collectors.toList());

						List<String> orgnrList = tilgangBrukerList.stream()
								.filter(tilgangBruker -> !tilgangBruker.isBrukerPerson())
								.map(TilgangBruker::getOrgnummer)
								.collect(Collectors.toList());

						return aktoerIdList.contains(arkivsak.getAktoerId()) || orgnrList.contains(arkivsak.getOrgnummer());
					})
					.map(arkivsak -> {
						safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
						return TilgangSak.builder()
								.aktoerId(arkivsak.getAktoerId())
								.orgnummer(arkivsak.getOrgnummer())
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(arkivsak.getArkivsaksystem().name())
								.tema(arkivsak.getTema().name())
								.build();
					}).collect(Collectors.toList());
		} catch (Exception e) {
			log.warn("findTilgangSakForGsaker feilet ved for fagsakIdInput={}.", fagsakIdInput);
		}
		return new ArrayList<>();
	}

	private List<TilgangSak> findTilgangSakForPsaker(List<TilgangBruker> tilgangBrukerList, FagsakIdInput fagsakIdInput, List<Tema> tema, SafRequestContext safRequestContext) {
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
								.fagsaksnummer(arkivsak.getFagsaksnummer())
								.fagsaksystem(arkivsak.getFagsaksystem())
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(arkivsak.getArkivsaksystem().name())
								.tema(arkivsak.getTema().name())
								.build();
					}).collect(Collectors.toList());

		} catch (Exception e) {
			log.warn("findTilgangSakForPsaker feilet ved for fagsakIdInput={}.", fagsakIdInput);
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
						safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
						return TilgangSak.builder()
								.aktoerId(arkivsak.getAktoerId())
								.orgnummer(arkivsak.getOrgnummer())
								.fagsaksnummer(arkivsak.getFagsaksnummer())
								.fagsaksystem(arkivsak.getFagsaksystem())
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(arkivsak.getArkivsaksystem().name())
								.tema(arkivsak.getTema().name())
								.build();
					});
		} catch (Exception e) {
			return Flowable.empty();
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
				log.warn("finnJournalposter feilet ved henting av journalposter på arkivsaker={}.",
						arkivsaksId, e);
			} else {
				log.warn("finnJournalposter feilet ved henting av journalposter på arkivsaker. Det var flere enn 1000 arkivsaker. Disse logges ikke da så lange logglinjer ikke støttes i logstash.", e);
			}
			return new ArrayList<>();
		}
	}



	private TilgangJournalpost mapTilgangJournalpost(JournalpostDto dto) {
		return TilgangJournalpost.builder()
				.journalpostId(dto.getJournalpostId().toString())
				.journalStatus(dto.getJournalstatus().toString())
				.journalpostType(dto.getJournalposttype().toString())
				.tema(dto.getFagomrade() == null ? null : dto.getFagomrade().toString())
				.datoOpprettet(dto.getDatoOpprettet().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
				.mottakskanal(dto.getMottakskanal() == null ? null : dto.getMottakskanal().toString())
				.avsenderMottakerId(dto.getAvsenderMottakerNavn())
				.dokumenter(dto.getDokumenter().stream().map(dokdto -> TilgangDokumentInfo.builder()
						.dokumentInfoId(dokdto.getDokumentInfoId())
						.dokumentstatus(dokdto.getDokumentstatus() == null ? null : dokdto.getDokumentstatus().toString())
						.brevkode(dokdto.getBrevkode())
						.variantFormat(dokdto.getVariantFormat() == null ? null : dokdto.getVariantFormat().toString())
						.build()).collect(Collectors.toList()))
				.build();
	}
}
