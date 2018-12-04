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
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;
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
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Repository
@Slf4j
public class TilgangsmodellRepositoryImpl implements TilgangsmodellRepository {
	public static final EnumSet<Tema> PENSJON = EnumSet.of(Tema.PEN, Tema.UFO);

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
			switch (brukerIdInput.getIdentType()) {
				case AKTOERID:
					return aktoerAntiCorruptionLayer.hentTilgangBrukerByAktoerId(brukerIdInput.getIdent());
				case FNR:
					return aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(brukerIdInput.getIdent());
				default:
					return null;
			}
		} catch (Exception e) {
			log.warn("findTilgangBruker feilet ved oppslag av ident. Brukertype={}", brukerIdInput.getIdentType(), e);
		}
		return null;
	}

	@Override
	@Cacheable(cacheNames = LokalCacheConfig.TILGANGSMODELL_REPO_SAK_CACHE, key = "#tilgangBruker.aktoerId + '_' + #tema")
	public List<TilgangSak> findTilgangSaker(final TilgangBruker tilgangBruker, final List<Tema> tema, SafRequestContext safRequestContext) {
		try {
			Observable<List<Arkivsak>> gsaker = Observable.fromCallable(() ->
					gsakAntiCorruptionLayer.findArkivsaker(tilgangBruker.getAktoerId(), tema))
					.subscribeOn(Schedulers.io());
			Observable<List<Arkivsak>> psaker = Observable.fromCallable(() -> {
				if (!Collections.disjoint(tema, PENSJON)) {
					return pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker.getFoedselsnr(), tema);
				} else {
					return new ArrayList<Arkivsak>();
				}
			}).subscribeOn(Schedulers.io());
			List<Arkivsak> arkivsaker = Observable.concat(gsaker, psaker)
					.flatMapIterable(items -> items)
					.toList().blockingGet();
			return arkivsaker.stream().map(arkivsak -> {
				safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
						return TilgangSak.builder()
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(arkivsak.getArkivsaksystem().name())
								.tema(arkivsak.getTema().name())
								.build();
					}
			).collect(Collectors.toList());
		} catch (Exception e) {
			return new ArrayList<>();
		}
	}

	@Override
	public List<TilgangJournalpost> findTilgangJournalposter(TilgangBruker tilgangBruker,
															 List<TilgangSak> tilgangSakList,
															 LocalDate fraDato,
															 List<Tema> inkluderTema, List<Journalposttype> inkluderJournalposttyper,
															 List<Journalstatus> inkluderJournalstatuses,
															 SafRequestContext safRequestContext) {
		try {
			List<JournalpostDto> journalposter = joarkAntiCorruptionLayer.hentJournalpostBulk(tilgangBruker,
					tilgangSakList,
					fraDato,
					inkluderTema,
					inkluderJournalposttyper,
					inkluderJournalstatuses);
			return journalposter.stream()
					.map(journalpostDto -> {
						safRequestContext.getRequestCache().putObject(journalpostDto.getJournalpostId().toString(), journalpostDto);
						return mapTilgangJournalpost(journalpostDto);
					})
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
