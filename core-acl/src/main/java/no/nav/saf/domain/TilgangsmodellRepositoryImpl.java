package no.nav.saf.domain;

import static java.lang.String.format;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoerid.AktoerAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalStatus;
import no.nav.saf.tjeneste.visningsmodell.kode.JournalpostType;
import no.nav.saf.tjeneste.visningsmodell.kode.Temakode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
	public TilgangBruker findTilgangBrukerByAktoerId(String aktoerId) {
		try {
			return aktoerAntiCorruptionLayer.hentTilgangBruker(aktoerId);
		} catch (Exception e) {
			log.warn(format("FindTilgangBrukerByAktoerId feilet ved oppslag av aktoer=%s. Feilmelding=%s", aktoerId, e.getMessage()));
		}
		return null;
	}

	@Override
	@Cacheable(cacheNames = LokalCacheConfig.TILGANGSMODELL_REPO_SAK_CACHE)
	public List<TilgangSak> findTilgangSakListByTilgangBruker(TilgangBruker tilgangBruker) {
		try {
			List<TilgangSak> tilgangSakList = gsakAntiCorruptionLayer.findTilgangSakListByAktoerId(tilgangBruker.getAktoerId());
			tilgangSakList.addAll(pensjonSakAntiCorruptionLayer.hentTilgangSakList(tilgangBruker.getFoedselsnr()));
			return tilgangSakList;
		} catch (Exception e) {
			log.warn(format("FindTilgangSakListByAktoerId feilet ved oppslag av aktoer=%s. Feilmelding=%s", tilgangBruker.getAktoerId(), e
					.getMessage()));
			return new ArrayList<>();
		}
	}

	@Override
	@Cacheable(cacheNames = LokalCacheConfig.TILGANGSMODELL_REPO_JORNALPOST_CACHE)
	public List<TilgangJournalpost> findTilgangJournalpostListByArkivsaker(List<TilgangSak> tilgangSakList) {
		try {
			return joarkAntiCorruptionLayer.hentTilgangJournalpostListByArkivsaker(tilgangSakList);
		} catch (Exception e) {
			log.warn(format("HentTilgangJournalpostListByArkivsaker feilet ved oppslag av arkivsaker=%s. Feilmelding=%s",
					tilgangSakList.stream().map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()), e.getMessage()));
			return new ArrayList<>();
		}
	}

	@Override
	public List<TilgangJournalpost> findTilgangJournalposter(TilgangBruker tilgangBruker, List<TilgangSak> tilgangSakList, LocalDate fraDato, Collection<Temakode> inkluderTema, List<JournalpostType> inkluderJournalposttyper, List<JournalStatus> inkluderJournalstatus) {
		try {
			return joarkAntiCorruptionLayer.hentTilgangJournalpostListByArkivsaker(tilgangBruker,
					tilgangSakList,
					fraDato,
					inkluderTema,
					inkluderJournalposttyper,
					inkluderJournalstatus);
		} catch (Exception e) {
			log.warn("HentTilgangJournalpostListByArkivsaker feilet ved oppslag av arkivsaker={}. Feilmelding={}",
					tilgangSakList.stream().map(TilgangSak::getArkivsaksnummer).collect(Collectors.toList()), e.getMessage());
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
	public TilgangBruker findTilgangBrukerBySakId(String sakId) {
		try {
			return gsakAntiCorruptionLayer.findTilgangSakBySakId(sakId);
		} catch (Exception e) {
			log.warn("findTilgangSakBySakId feilet ved oppslag på sakId={}", sakId);
			return null;
		}
	}
}
