package no.nav.saf.domain;

import io.reactivex.Observable;
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
import no.nav.saf.tjeneste.visningsmodell.Brukeridentifikator;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import no.nav.saf.tjeneste.visningsmodell.kode.Journalz;
import no.nav.saf.tjeneste.visningsmodell.kode.Journazz;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
@Slf4j
public class TilgangsmodellRepositoryImpl implements TilgangsmodellRepository {

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
	public TilgangBruker findTilgangBruker(Brukeridentifikator brukeridentifikator) {
		try {
			switch (brukeridentifikator.getIdentType()) {
				case AKTOERID:
					return aktoerAntiCorruptionLayer.hentTilgangBrukerByAktoerId(brukeridentifikator.getIdent());
				case FOEDSELSNUMMER:
					return aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(brukeridentifikator.getIdent());
				default:
					return null;
			}
		} catch (Exception e) {
			log.warn("findTilgangBruker feilet ved oppslag av ident. Brukertype={}", brukeridentifikator.getIdentType(), e);
		}
		return null;
	}

	@Override
	@Cacheable(cacheNames = LokalCacheConfig.TILGANGSMODELL_REPO_SAK_CACHE, key = "#tilgangBruker.aktoerId")
	public List<TilgangSak> findTilgangSakListByTilgangBruker(final TilgangBruker tilgangBruker) {
		try {
			Observable<List<TilgangSak>> gsaker = Observable.fromCallable(() ->
					gsakAntiCorruptionLayer.findTilgangSakListByAktoerId(tilgangBruker.getAktoerId()))
					.subscribeOn(Schedulers.io());
			Observable<List<TilgangSak>> psaker = Observable.fromCallable(() ->
					pensjonSakAntiCorruptionLayer.hentTilgangSakList(tilgangBruker.getFoedselsnr()))
					.subscribeOn(Schedulers.io());
			return Observable.concat(gsaker, psaker)
					.flatMapIterable(item -> item)
					.toList().blockingGet();
		} catch (Exception e) {
			log.warn("FindTilgangSakListByAktoerId feilet ved oppslag av aktoer={}", tilgangBruker.getAktoerId(), e);
			return new ArrayList<>();
		}
	}

	@Override
	public List<TilgangJournalpost> findTilgangJournalposter(SafRequestContext safRequestContext,
															 TilgangBruker tilgangBruker,
															 List<TilgangSak> tilgangSakList,
															 LocalDate fraDato,
															 List<Journalz> inkluderJournalposttyper,
															 List<Journazz> inkluderJournalstatuses) {
		try {
			Map<String, JournalpostDto> journalpostMap = joarkAntiCorruptionLayer.hentJournalpostBulk(tilgangBruker,
					tilgangSakList,
					fraDato,
					inkluderJournalposttyper,
					inkluderJournalstatuses);
			safRequestContext.getParameterContext().putParameters(journalpostMap);
			return journalpostMap.entrySet().stream()
					.map(stringJournalpostDtoEntry -> mapTilgangJournalpost(stringJournalpostDtoEntry.getValue()))
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.warn("HentTilgangJournalpostListByArkivsaker feilet ved oppslag av arkivsaker={}.",
					tilgangSakList.stream().map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()), e);
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
