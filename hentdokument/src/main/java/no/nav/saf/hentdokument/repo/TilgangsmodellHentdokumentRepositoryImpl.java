package no.nav.saf.hentdokument.repo;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.fpsak.FpsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
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

import static no.nav.saf.domain.kode.Arkivsakssystem.GSAK;
import static no.nav.saf.domain.kode.Arkivsakssystem.PSAK;
import static no.nav.saf.domain.kode.Tema.PEN;
import static no.nav.saf.domain.kode.Tema.UFO;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Repository
public class TilgangsmodellHentdokumentRepositoryImpl implements TilgangsmodellHentdokumentRepository {

	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final HentDokumentAntiCorruptionLayer hentDokumentAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;
	private final FpsakAntiCorruptionLayer fpsakAntiCorruptionLayer;

	@Inject
	public TilgangsmodellHentdokumentRepositoryImpl(PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
													HentDokumentAntiCorruptionLayer hentDokumentAntiCorruptionLayer,
													BisysAntiCorruptionLayer bisysAntiCorruptionLayer,
													FpsakAntiCorruptionLayer fpsakAntiCorruptionLayer) {
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.hentDokumentAntiCorruptionLayer = hentDokumentAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
		this.fpsakAntiCorruptionLayer = fpsakAntiCorruptionLayer;
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
			return findTilgangBrukerByArkivsak(arkivsak, safRequestContext);
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

	public TilgangBruker findTilgangBrukerByArkivsak(Arkivsak arkivsak, SafRequestContext safRequestContext) {
		try {
			if (GSAK.equals(arkivsak.getArkivsaksystem())) {
				TilgangBruker gsakTilgangBruker = TilgangBruker.builder()
						.aktoerId(arkivsak.getAktoerId())
						.orgnummer(arkivsak.getAktoerId() == null ? arkivsak.getOrgnummer() : null)
						.build();
				// GSAK har ikke aktørId så vi har bruker på journalposten for sporing
				TilgangBruker journalpostTilgangBruker = hentDokumentAntiCorruptionLayer.hentTilgangBruker(safRequestContext);
				return gsakTilgangBruker.toBuilder()
						.foedselsnr(journalpostTilgangBruker.getFoedselsnr())
						.build();
			} else if (PSAK.equals(arkivsak.getArkivsaksystem())) {
				String fnr = pensjonSakAntiCorruptionLayer.findFoedselsnummerBySakId(arkivsak.getArkivsaksnummer());
				if (fnr == null) {
					return null;
				} else {
					return TilgangBruker.builder()
							.foedselsnr(fnr)
							.build();
				}
			} else {
				return null;
			}
		} catch (Exception e) {
			log.warn("findTilgangBrukerBySakId feilet ved oppslag på sakId={} og arkivsaksystem={}. Feilmelding={}", arkivsak.getArkivsaksnummer(), arkivsak.getArkivsaksystem(), e);
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
			if (arkivsak == null || tilgangBruker == null) {
				return null;
			}
			if (GSAK == arkivsak.getArkivsaksystem()) {
				BidragSak bidragSak = bisysAntiCorruptionLayer.hentBidragSakByArkivsak(arkivsak);
				List<String> fpsak = fpsakAntiCorruptionLayer.hentRelevanteParter(arkivsak);
				return TilgangSak.builder()
						.aktoerId(arkivsak.getAktoerId())
						.arkivsaksnummer(arkivsak.getArkivsaksnummer())
						.arkivsaksystem(GSAK)
						.tema(arkivsak.getTema())
						.orgnummer(arkivsak.getOrgnummer())
						.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
						.fagsaksystem(arkivsak.getFagsaksystem())
						.fpAktoerIdList(fpsak)
						.build();
			} else if (PSAK == arkivsak.getArkivsaksystem()) {
				List<Arkivsak> arkivsaker = pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker, Arrays.asList(PEN, UFO));
				for (Arkivsak psakArkivsak : arkivsaker) {
					if (psakArkivsak.getArkivsaksnummer().equals(arkivsak.getArkivsaksnummer())) {
						return TilgangSak.builder()
								.aktoerId(psakArkivsak.getAktoerId())
								.arkivsaksnummer(psakArkivsak.getArkivsaksnummer())
								.arkivsaksystem(PSAK)
								.tema(psakArkivsak.getTema())
								.orgnummer(psakArkivsak.getOrgnummer())
								.relevanteTredjeparter(new ArrayList<>())
								.fagsaksystem(psakArkivsak.getFagsaksystem())
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
}
