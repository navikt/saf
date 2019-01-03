package no.nav.saf.hentdokument.repo;

import static no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayerImpl.TEMA_PENSJON;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.AktoerAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.joark.JoarkAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.visningsmodell.kode.Arkivsakssystem;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Slf4j
@Repository
public class TilgangsmodellHentdokumentRepositoryImpl implements TilgangsmodellHentdokumentRepository {

	private final AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer;
	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final JoarkAntiCorruptionLayer joarkAntiCorruptionLayer;

	@Inject
	public TilgangsmodellHentdokumentRepositoryImpl(AktoerAntiCorruptionLayer aktoerAntiCorruptionLayer,
													GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
													PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
													JoarkAntiCorruptionLayer joarkAntiCorruptionLayer) {
		this.aktoerAntiCorruptionLayer = aktoerAntiCorruptionLayer;
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.joarkAntiCorruptionLayer = joarkAntiCorruptionLayer;
	}

	@Override
	public TilgangJournalpost findTilgangJournalpostFromSafRequestContext(SafRequestContext safRequestContext) {
		return joarkAntiCorruptionLayer.hentTilgangJournalpostFromSafRequestContext(safRequestContext);
	}

	@Override
	public TilgangBruker findTilgangBruker(Arkivsak arkivsak, SafRequestContext safRequestContext) {
		if (arkivsak == null) {
			return null;
		} else if (arkivsak.getArkivsaksnummer() == null || arkivsak.getArkivsaksnummer().isEmpty()) {
			//Midlertidig journalpost - ingen arkivsaksnummer
			return findTilgangBrukerBrukerFromSafRequestContext(safRequestContext);
		} else {
			//Henter Bruker fra gsak eller psak
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
			log.warn("findTilgangBrukerBySakId feilet ved oppslag på sakId={} og arkivsaksystem={}. Feilmelding={}", sakId, arkivsaksystem, e
					.getMessage());
			return null;
		}
	}

	@Override
	public Arkivsak findArkivsakAndCacheJournalpostDto(String journalpostId, String dokumentId, String variantFormat, SafRequestContext safRequestContext) {
		return joarkAntiCorruptionLayer.hentArkivsakAndCacheJournalpostDto(journalpostId, dokumentId, variantFormat, safRequestContext);
	}

	@Override
	public TilgangSak findTilgangSak(String sakId, String arkivsaksystem, TilgangBruker tilgangBruker, SafRequestContext safRequestContext) {
		try {
			if (Arkivsakssystem.GSAK.name().equals(arkivsaksystem)) {
				return gsakAntiCorruptionLayer.findTilgangSakBySakId(sakId);
			} else if (Arkivsakssystem.PSAK.name().equals(arkivsaksystem)) {
				return findTilgangSakFromPsak(sakId, tilgangBruker);
			} else if (arkivsaksystem == null || arkivsaksystem.isEmpty()) {
				//Midlertidig journalført
				return joarkAntiCorruptionLayer.hentTilgangSakFromSafRequestContext(safRequestContext);
			} else {
				return null;
			}
		} catch (Exception e) {
			log.warn("findTilgangBrukerBySakId feilet ved oppslag på sakId={} og arkivsaksystem={}. Feilmelding={}", sakId, arkivsaksystem, e
					.getMessage());
			return null;
		}
	}

	private TilgangSak findTilgangSakFromPsak(String sakId, TilgangBruker tilgangBruker) {
		Arkivsak arkivsak = pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker, new ArrayList<>(TEMA_PENSJON))
				.stream()
				.filter(sak -> sak.getArkivsaksnummer() == sakId)
				.findAny()
				.orElse(null);

		if (arkivsak == null) {
			return null;
		} else {
			return TilgangSak.builder()
					.aktoerId(arkivsak.getAktoerId())
					.fagsaksnummer(arkivsak.getFagsaksnummer())
					.fagsaksystem(arkivsak.getFagsaksystem())
					.arkivsaksnummer(arkivsak.getArkivsaksnummer())
					.arkivsaksystem(arkivsak.getArkivsaksystem().name())
					.tema(arkivsak.getTema().name())
					.build();
		}
	}
}
