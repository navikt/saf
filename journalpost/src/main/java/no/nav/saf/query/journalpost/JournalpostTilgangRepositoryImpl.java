package no.nav.saf.query.journalpost;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.PdlAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static no.nav.saf.domain.kode.Arkivsakssystem.GSAK;
import static no.nav.saf.domain.kode.Arkivsakssystem.PSAK;
import static no.nav.saf.domain.kode.Tema.PEN;
import static no.nav.saf.domain.kode.Tema.UFO;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class JournalpostTilgangRepositoryImpl implements JournalpostTilgangRepository {

	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final JournalpostAntiCorruptionLayer journalpostAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;
	private final PdlAntiCorruptionLayer aktoerAntiCorruptionLayer;

	@Inject
	public JournalpostTilgangRepositoryImpl(PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
											JournalpostAntiCorruptionLayer journalpostAntiCorruptionLayer,
											BisysAntiCorruptionLayer bisysAntiCorruptionLayer,
											PdlAntiCorruptionLayer aktoerAntiCorruptionLayer) {
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
			return findTilgangBrukerByArkivsak(arkivsak);
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

	public TilgangBruker findTilgangBrukerByArkivsak(Arkivsak arkivsak) {
		try {
			if (GSAK.equals(arkivsak.getArkivsaksystem())) {
				return TilgangBruker.builder()
						.aktoerId(arkivsak.getAktoerId())
						.orgnummer(arkivsak.getAktoerId() == null ? arkivsak.getOrgnummer() : null)
						.build();
			} else if (PSAK.equals(arkivsak.getArkivsaksystem())) {
				// Slår opp i PSAK for å finne fnr på bruker. Deretter opp i aktoerregister for fnr -> aktørId
				String fnr = pensjonSakAntiCorruptionLayer.findFoedselsnummerBySakId(arkivsak.getArkivsaksnummer());
				return aktoerAntiCorruptionLayer.hentTilgangBrukerByFoedselsnummer(fnr);
			} else {
				return null;
			}
		} catch (Exception e) {
			log.warn("findTilgangBrukerBySakId feilet ved oppslag på sakId={} og arkivsaksystem={}. Feilmelding={}",
					arkivsak.getArkivsaksnummer(), arkivsak.getArkivsaksystem(), e);
			return null;
		}
	}

	@Override
	public Arkivsak findArkivsakAndCacheJournalpostDto(String journalpostId, SafRequestContext safRequestContext) {
		return journalpostAntiCorruptionLayer.hentArkivsakAndCacheJournalpostDto(journalpostId, safRequestContext);
	}

	@Override
	public TilgangSak findTilgangSak(Arkivsak arkivsak, TilgangBruker tilgangBruker, SafRequestContext safRequestContext) {
		if (arkivsak == null) {
			return null;
		}
		try {
			String arkivSystem = Optional.ofNullable(arkivsak.getArkivsaksystem()).map(Object::toString).orElse(null);
			if (GSAK.name().equals(arkivSystem)) {
				safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
				BidragSak bidragSak = bisysAntiCorruptionLayer.hentBidragSakByArkivsak(arkivsak);
				return TilgangSak.builder()
						.aktoerId(arkivsak.getAktoerId())
						.arkivsaksnummer(arkivsak.getArkivsaksnummer())
						.arkivsaksystem(GSAK)
						.fagsaksystem(arkivsak.getFagsaksystem())
						.tema(arkivsak.getTema())
						.orgnummer(arkivsak.getOrgnummer())
						.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
						.build();
			} else if (PSAK.name().equals(arkivSystem)) {
				List<Arkivsak> arkivsaker = pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker, Arrays.asList(PEN, UFO));
				for (Arkivsak pensjonArkivsak : arkivsaker) {
					if (pensjonArkivsak.getArkivsaksnummer().equals(arkivsak.getArkivsaksnummer())) {
						safRequestContext.getRequestCache().putObject(pensjonArkivsak.getKey(), pensjonArkivsak);
						return TilgangSak.builder()
								.aktoerId(pensjonArkivsak.getAktoerId())
								.arkivsaksnummer(pensjonArkivsak.getArkivsaksnummer())
								.arkivsaksystem(PSAK)
								.tema(pensjonArkivsak.getTema())
								.orgnummer(pensjonArkivsak.getOrgnummer())
								.relevanteTredjeparter(new ArrayList<>())
								.build();
					}
				}
			}
			// fallback
			return journalpostAntiCorruptionLayer.hentTilgangSakFromSafRequestContext(safRequestContext, tilgangBruker);
		} catch (Exception e) {
			log.warn("findTilgangBrukerBySakId feilet ved oppslag på sakId={} og arkivsaksystem={}. Feilmelding={}", arkivsak.getArkivsaksnummer(), arkivsak.getArkivsaksystem(), e);
			return null;
		}
	}
}
