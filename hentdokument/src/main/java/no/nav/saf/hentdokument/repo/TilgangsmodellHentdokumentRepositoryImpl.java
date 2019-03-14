package no.nav.saf.hentdokument.repo;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.hentdokument.HentDokumentAntiCorruptionLayer;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Repository
public class TilgangsmodellHentdokumentRepositoryImpl implements TilgangsmodellHentdokumentRepository {

	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final HentDokumentAntiCorruptionLayer hentDokumentAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;

	@Inject
	public TilgangsmodellHentdokumentRepositoryImpl(GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
													PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
													HentDokumentAntiCorruptionLayer hentDokumentAntiCorruptionLayer,
													BisysAntiCorruptionLayer bisysAntiCorruptionLayer) {
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.hentDokumentAntiCorruptionLayer = hentDokumentAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
	}

	@Override
	public TilgangJournalpost findTilgangJournalpostFromSafRequestContext(SafRequestContext safRequestContext) {
		try {
			return hentDokumentAntiCorruptionLayer.hentTilgangJournalpostFromSafRequestContext(safRequestContext);
		} catch (Exception e) {
			log.warn("findTilgangJournalpostFromSafRequestContext feilet", e);
			return null;
		}
	}

	@Override
	public TilgangBruker findTilgangBruker(Arkivsak arkivsak, SafRequestContext safRequestContext) {
		if (arkivsak == null) {
			return null;
		} else if (arkivsak.getArkivsaksnummer() == null || arkivsak.getArkivsaksnummer().isEmpty()) {
			//Midlertidig journalpost - ingen arkivsaksnummer
			return findTilgangBrukerBrukerFromSafRequestContext(safRequestContext);
		} else {
			//Bruker hentes fra gsak eller psak
			return findTilgangBrukerBySakId(arkivsak.getArkivsaksnummer(), arkivsak.getArkivsaksystem());
		}
	}

	private TilgangBruker findTilgangBrukerBrukerFromSafRequestContext(SafRequestContext safRequestContext) {
		try {
			TilgangBruker tilgangBruker = hentDokumentAntiCorruptionLayer.hentTilgangBruker(safRequestContext);
			if (tilgangBruker == null || tilgangBruker.getFoedselsnr() == null) {
				return null;
			} else {
				return tilgangBruker;
			}
		} catch (Exception e) {
			log.warn("findTilgangBrukerBrukerFromSafRequestContext feilet", e);
			return null;
		}
	}

	public TilgangBruker findTilgangBrukerBySakId(String sakId, Arkivsakssystem arkivsaksystem) {
		try {
			if (Arkivsakssystem.GSAK.equals(arkivsaksystem)) {
				return gsakAntiCorruptionLayer.findTilgangBrukerBySakId(sakId);
			} else if (Arkivsakssystem.PSAK.equals(arkivsaksystem)) {
				String fnr = pensjonSakAntiCorruptionLayer.findFoedselsnummerBySakId(sakId);
				return TilgangBruker.builder()
						.foedselsnr(fnr)
						.build();
			} else {
				return null;
			}
		} catch (Exception e) {
			log.warn("findTilgangBrukerBySakId feilet ved oppslag på sakId={} og arkivsaksystem={}. Feilmelding={}", sakId, arkivsaksystem, e);
			return null;
		}
	}

	@Override
	public Arkivsak findArkivsakAndCacheJournalpostDto(String journalpostId, String dokumentInfoId, String variantFormat, SafRequestContext safRequestContext) {
		return hentDokumentAntiCorruptionLayer.hentArkivsakAndCacheJournalpostDto(journalpostId, dokumentInfoId, variantFormat, safRequestContext);
	}

	@Override
	public TilgangSak findTilgangSak(Arkivsak arkivsak, TilgangBruker tilgangBruker, SafRequestContext safRequestContext) {
		try {
			if(arkivsak == null || tilgangBruker == null) {
				return null;
			}
			if (Arkivsakssystem.GSAK == arkivsak.getArkivsaksystem()) {
				Arkivsak gsakArkivsak = gsakAntiCorruptionLayer.findArkivsakBySakId(arkivsak.getArkivsaksnummer());
				BidragSak bidragSak = getBidragSakIfTemaIsBidOrFar(gsakArkivsak);
				return TilgangSak.builder()
						.aktoerId(gsakArkivsak.getAktoerId())
						.arkivsaksnummer(gsakArkivsak.getArkivsaksnummer())
						.arkivsaksystem(Arkivsakssystem.GSAK)
						.tema(gsakArkivsak.getTema())
						.orgnummer(gsakArkivsak.getOrgnummer())
						.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
						.paragraf19(bidragSak != null && bidragSak.isParagraf19())
						.build();
			} else if (Arkivsakssystem.PSAK == arkivsak.getArkivsaksystem()) {
				List<Arkivsak> arkivsaker = pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker, Arrays.asList(Tema.PEN, Tema.UFO));
				for (Arkivsak psakArkivsak : arkivsaker) {
					if (psakArkivsak.getArkivsaksnummer().equals(arkivsak.getArkivsaksnummer())) {
						return TilgangSak.builder()
								.aktoerId(psakArkivsak.getAktoerId())
								.arkivsaksnummer(psakArkivsak.getArkivsaksnummer())
								.arkivsaksystem(Arkivsakssystem.PSAK)
								.tema(psakArkivsak.getTema())
								.orgnummer(psakArkivsak.getOrgnummer())
								.relevanteTredjeparter(new ArrayList<>())
								.paragraf19(false)
								.build();
					}
				}
				// fallback
				return hentDokumentAntiCorruptionLayer.hentTilgangSakFromSafRequestContext(safRequestContext, tilgangBruker);
			} else {
				return hentDokumentAntiCorruptionLayer.hentTilgangSakFromSafRequestContext(safRequestContext, tilgangBruker);
			}
		} catch (Exception e) {
			log.warn("findTilgangBrukerBySakId feilet ved oppslag på sakId={} og arkivsaksystem={}. Feilmelding={}", arkivsak.getArkivsaksnummer(), arkivsak.getArkivsaksystem(), e);
			return null;
		}
	}

	private BidragSak getBidragSakIfTemaIsBidOrFar(Arkivsak arkivsak) {
		if (Tema.BID.equals(arkivsak.getTema()) || Tema.FAR.equals(arkivsak.getTema())) {
			return bisysAntiCorruptionLayer.hentBidragSak(arkivsak.getFagsakId());
		} else {
			return new BidragSak();
		}
	}

}
