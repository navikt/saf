package no.nav.saf.query.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.AktoerAntiCorruptionLayer;
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
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class JournalpostTilgangRepositoryImpl implements JournalpostTilgangRepository {

	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final JournalpostAntiCorruptionLayer journalpostAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;
	private final AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer;

	@Inject
	public JournalpostTilgangRepositoryImpl(GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
											PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
											JournalpostAntiCorruptionLayer journalpostAntiCorruptionLayer,
											BisysAntiCorruptionLayer bisysAntiCorruptionLayer,
											AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer) {
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.journalpostAntiCorruptionLayer = journalpostAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
		this.aktoerAntiCorruptionLayer = aktoerAntiCorruptionLayer;
	}

	@Override
	public TilgangJournalpost findTilgangJournalpostFromSafRequestContext(SafRequestContext safRequestContext, TilgangSak tilgangSak) {
		try {
			return journalpostAntiCorruptionLayer.hentTilgangJournalpostFromSafRequestContext(safRequestContext);
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
			TilgangBruker tilgangBruker = journalpostAntiCorruptionLayer.hentTilgangBruker(safRequestContext);
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
				// Slår opp i PSAK for å finne fnr på bruker. Deretter opp i aktoerregister for fnr -> aktørId
				String fnr = pensjonSakAntiCorruptionLayer.findFoedselsnummerBySakId(sakId);
				return aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(fnr);
			} else {
				return null;
			}
		} catch (Exception e) {
			log.warn("findTilgangBrukerBySakId feilet ved oppslag på sakId={} og arkivsaksystem={}. Feilmelding={}", sakId, arkivsaksystem, e);
			return null;
		}
	}

	@Override
	public Arkivsak findArkivsakAndCacheJournalpostDto(String journalpostId, SafRequestContext safRequestContext) {
		return journalpostAntiCorruptionLayer.hentArkivsakAndCacheJournalpostDto(journalpostId, safRequestContext);
	}

	@Override
	public TilgangSak findTilgangSak(String sakId, String arkivsaksystem, TilgangBruker tilgangBruker, SafRequestContext safRequestContext) {
		try {
			if (Arkivsakssystem.GSAK.name().equals(arkivsaksystem)) {
				Arkivsak arkivsak = gsakAntiCorruptionLayer.findArkivsakBySakId(sakId);
				safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
				BidragSak bidragSak = getBidragSakIfTemaIsBidOrFar(arkivsak);
				return TilgangSak.builder()
						.aktoerId(arkivsak.getAktoerId())
						.arkivsaksnummer(arkivsak.getArkivsaksnummer())
						.arkivsaksystem(Arkivsakssystem.GSAK)
						.tema(arkivsak.getTema())
						.orgnummer(arkivsak.getOrgnummer())
						.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
						.paragraf19(bidragSak == null ? null : bidragSak.isParagraf19())
						.build();
			} else if (Arkivsakssystem.PSAK.name().equals(arkivsaksystem)) {
				List<Arkivsak> arkivsaker = pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker, Arrays.asList(Tema.PEN, Tema.UFO));
				for(Arkivsak arkivsak : arkivsaker) {
					if(arkivsak.getArkivsaksnummer().equals(sakId)) {
						safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
						return TilgangSak.builder()
								.aktoerId(arkivsak.getAktoerId())
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(Arkivsakssystem.PSAK)
								.tema(arkivsak.getTema())
								.orgnummer(arkivsak.getOrgnummer())
								.relevanteTredjeparter(new ArrayList<>())
								.paragraf19(false)
								.build();
					}
				}
				return null;
			} else {
				return journalpostAntiCorruptionLayer.hentTilgangSakFromSafRequestContext(safRequestContext, tilgangBruker);
			}
		} catch (Exception e) {
			log.warn("findTilgangBrukerBySakId feilet ved oppslag på sakId={} og arkivsaksystem={}. Feilmelding={}", sakId, arkivsaksystem, e);
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
