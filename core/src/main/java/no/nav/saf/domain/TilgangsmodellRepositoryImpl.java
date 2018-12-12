package no.nav.saf.domain;

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
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
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
import java.util.EnumSet;
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
	public static final EnumSet<Tema> PENSJON = EnumSet.of(Tema.PEN, Tema.UFO);
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
			log.warn("findTilgangBrukerList feilet ved oppslag. fagsakIdInput={}", fagsakIdInput, e);
		}
		return new ArrayList<>();
	}

	@Override
	@Cacheable(cacheNames = LokalCacheConfig.TILGANGSMODELL_REPO_SAK_CACHE)
	public List<TilgangSak> findTilgangSaker(final FagsakIdInput fagsakIdInput, final List<Tema> tema, final SafRequestContext safRequestContext) {
		try {
			List<Arkivsak> arkivsaker = gsakAntiCorruptionLayer.findTilgangSakListByFagsakIdAndFagsaksystem(fagsakIdInput.getFagsaksnummer(), fagsakIdInput.getFagsaksystem(), tema);
			return arkivsaker.stream().map(arkivsak -> {
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
			log.warn("findTilgangSakList feilet ved for fagsakId={}.", fagsakIdInput);
		}
		return new ArrayList<>();
	}

	@Override
	@Cacheable(cacheNames = LokalCacheConfig.TILGANGSMODELL_REPO_SAK_CACHE, key = "#tilgangBruker.aktoerId + '_' + #tilgangBruker.orgnummer + '_' + #tema")
	public Flowable<TilgangSak> findTilgangSaker(final TilgangBruker tilgangBruker, final List<Tema> tema, final SafRequestContext safRequestContext) {
		try {
			Flowable<List<Arkivsak>> gsakerFromOrgnr = Flowable.fromCallable(() ->
					gsakAntiCorruptionLayer.findArkivsakerByOrgnr(tilgangBruker.getOrgnummer(), tema))
					.subscribeOn(Schedulers.io());
			Flowable<List<Arkivsak>> gsakerFromAktoerId = Flowable.fromCallable(() ->
					gsakAntiCorruptionLayer.findArkivsakerByAktoerId(tilgangBruker.getAktoerId(), tema))
					.subscribeOn(Schedulers.io());
			Flowable<List<Arkivsak>> psaker = Flowable.fromCallable(() -> {
				if (!Collections.disjoint(tema, PENSJON)) {
					return pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker.getFoedselsnr(), tema);
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
			List<String> identer = tilgangBrukere.stream().flatMap(t -> t.getAlleIdenter().stream()).collect(Collectors.toList());
			List<JournalpostDto> journalposter = joarkAntiCorruptionLayer.finnJournalposter(identer,
					tilgangSakList, fraDato, inkluderTema, inkluderJournalposttyper, inkluderJournalstatuses, foerste, etterPeker, siste, foerPeker);
			return journalposter.stream()
					.map(journalpostDto -> {
						safRequestContext.getRequestCache().putObject(journalpostDto.getJournalpostId().toString(), journalpostDto);
						return mapTilgangJournalpost(journalpostDto);
					})
					.collect(Collectors.toList());
		} catch (Exception e) {
			if (tilgangSakList.size() < MAX_ARKIVSAKER_LOGG) {
				List<String> arkivsaksId = tilgangSakList.stream().map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList());
				log.warn("finnJournalposter feilet ved henting av journalposter på arkivsaker={}.",
						arkivsaksId, e);
			} else {
				log.warn("finnJournalposter feilet ved henting av journalposter på arkivsaker. Det var flere enn 1000 arkivsaker. Disse logges ikke da så lange logglinjer ikke støttes i logstash.", e);
			}
			return new ArrayList<>();
		}
	}

	@Override
	public TilgangJournalpost findTilgangJournalpost(String journalpostId, String dokumentId, String variantFormat) {
		try {
			return joarkAntiCorruptionLayer.hentTilgangJournalpost(journalpostId, dokumentId, variantFormat);
		} catch (Exception e) {
			log.warn("hentTilgangJournalpost feilet ved oppslag, journalpostId={}, dokumentId={}, variantFormat={}. Feilmelding={}",
					journalpostId, dokumentId, variantFormat, e.getMessage());
		}
		return null;
	}

	@Override
	public TilgangDokumentInfo findTilgangDokumentInfo(String journalpostId, String dokumentId, String variantFormat) {
		try {
			return joarkAntiCorruptionLayer.hentTilgangDokumentInfo(journalpostId, dokumentId, variantFormat);
		} catch (Exception e) {
			log.warn("hentTilgangDokumentInfo feilet ved oppslag, journalpostId={}, dokumentId={}, variantFormat={}. Feilmelding={}",
					journalpostId, dokumentId, variantFormat, e.getMessage());
		}
		return null;
	}

	@Override
	public TilgangSak findTilgangSak(String journalpostId, String dokumentId, String variantFormat) {
		try {
			return joarkAntiCorruptionLayer.hentTilgangSak(journalpostId, dokumentId, variantFormat);
		} catch (Exception e) {
			log.warn("hentTilgangSak feilet ved oppslag, journalpostId={}, dokumentId={}, variantFormat={}. Feilmelding={}",
					journalpostId, dokumentId, variantFormat, e.getMessage());
		}
		return null;
	}

	@Override
	public TilgangBruker findTilgangBruker(String journalpostId, String dokumentId, String variantFormat) {
		try {
			return joarkAntiCorruptionLayer.hentTilgangBruker(journalpostId, dokumentId, variantFormat);
		} catch (Exception e) {
			log.warn("hentTilgangBruker feilet ved oppslag, journalpostId={}, dokumentId={}, variantFormat={}. Feilmelding={}",
					journalpostId, dokumentId, variantFormat, e.getMessage());
		}
		return null;
	}

	@Override
	public TilgangBruker findTilgangBrukerBySakId(String sakId, String arkivsaksystem) {
		try {
			if (Arkivsakssystem.GSAK.name().equals(arkivsaksystem)) {
				return gsakAntiCorruptionLayer.findTilgangBrukerBySakId(sakId);
			} else if (Arkivsakssystem.PSAK.name().equals(arkivsaksystem)) {
				//TODO implement call to psak
				return null;
			} else {
				return null;
			}
		} catch (Exception e) {
			log.warn("findTilgangBrukerBySakId feilet ved oppslag på sakId={}. Feilmelding={}", sakId, e.getMessage());
			return null;
		}
	}

	@Override
	public TilgangSak findTilgangSakBySakId(String sakId, String arkivsaksystem) {
		try {
			if (Arkivsakssystem.GSAK.name().equals(arkivsaksystem)) {
				return gsakAntiCorruptionLayer.findTilgangSakBySakId(sakId);
			} else if (Arkivsakssystem.PSAK.name().equals(arkivsaksystem)) {
				//TODO implement call to psak
				return null;
			} else {
				return null;
			}
		} catch (Exception e) {
			log.warn("findTilgangBrukerBySakId feilet ved oppslag på sakId={}. Feilmelding={}", sakId, e.getMessage());
			return null;
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
