package no.nav.saf.hentdokument.repo;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Repository
public class TilgangsmodellHentdokumentRepositoryImpl implements TilgangsmodellHentdokumentRepository {

	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final JoarkAntiCorruptionLayer joarkAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;

	@Inject
	public TilgangsmodellHentdokumentRepositoryImpl(GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
													PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
													JoarkAntiCorruptionLayer joarkAntiCorruptionLayer, BisysAntiCorruptionLayer bisysAntiCorruptionLayer) {
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.joarkAntiCorruptionLayer = joarkAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
	}

	@Override
	public TilgangJournalpost findTilgangJournalpostFromSafRequestContext(SafRequestContext safRequestContext, TilgangSak tilgangSak) {
		return joarkAntiCorruptionLayer.hentTilgangJournalpostFromSafRequestContext(safRequestContext, tilgangSak);
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
		TilgangBruker tilgangBruker = joarkAntiCorruptionLayer.hentTilgangBruker(safRequestContext);
		if (tilgangBruker == null || tilgangBruker.getFoedselsnr() == null) {
			return null;
		} else {
			return tilgangBruker;
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
		return joarkAntiCorruptionLayer.hentArkivsakAndCacheJournalpostDto(journalpostId, dokumentInfoId, variantFormat, safRequestContext);
	}

	@Override
	public TilgangSak findTilgangSak(String sakId, String arkivsaksystem, TilgangBruker tilgangBruker, SafRequestContext safRequestContext) {
		try {
			if (Arkivsakssystem.GSAK.name().equals(arkivsaksystem)) {
				Arkivsak arkivsak = gsakAntiCorruptionLayer.findArkivsakBySakId(sakId);
				BidragSak bidragSak = getBidragSakIfTemaIsBidOrFar(arkivsak);
				return TilgangSak.builder()
						.aktoerId(arkivsak.getAktoerId())
						.arkivsaksnummer(arkivsak.getArkivsaksnummer())
						.arkivsaksystem(Arkivsakssystem.GSAK)
						.fagsaksystem(arkivsak.getFagsaksystem())
						.fagsakId(arkivsak.getFagsakId())
						.tema(arkivsak.getTema() == null ? null : arkivsak.getTema().name())
						.orgnummer(arkivsak.getOrgnummer())
						.relevanteTredjeparter(new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
						.paragraf19(bidragSak.isParagraf19())
						.build();
			} else if (Arkivsakssystem.PSAK.name().equals(arkivsaksystem)
					|| arkivsaksystem == null || arkivsaksystem.isEmpty()) {
				//Psak eller midlertidig journalført
				return joarkAntiCorruptionLayer.hentTilgangSakFromSafRequestContext(safRequestContext, tilgangBruker);
			} else {
				return null;
			}
		} catch (
				Exception e) {
			log.warn("findTilgangBrukerBySakId feilet ved oppslag på sakId={} og arkivsaksystem={}. Feilmelding={}", sakId, arkivsaksystem, e
					.getMessage());
			return null;
		}
	}

	private BidragSak getBidragSakIfTemaIsBidOrFar(Arkivsak arkivsak) {
		if (Tema.BID.equals(arkivsak.getTema()) || Tema.FAR.equals(arkivsak.getTema())) {
			return bisysAntiCorruptionLayer.hentBidragSak(arkivsak.getArkivsaksnummer(), null);
		} else {
			return new BidragSak();
		}
	}

}
