package no.nav.saf.query.dokumentoversikt.bruker;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.aktoer.PdlAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component
public class DokumentoversiktBrukerTilgangsmodellRepository {
	private static final Set<Tema> TEMA_PENSJON = EnumSet.of(Tema.PEN, Tema.UFO);

	private final PdlAntiCorruptionLayer aktoerAntiCorruptionLayer;
	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;

	@Inject
	public DokumentoversiktBrukerTilgangsmodellRepository(PdlAntiCorruptionLayer aktoerAntiCorruptionLayer,
														  GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
														  PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
														  BisysAntiCorruptionLayer bisysAntiCorruptionLayer) {
		this.aktoerAntiCorruptionLayer = aktoerAntiCorruptionLayer;
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
	}

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

	public Flowable<TilgangSak> findTilgangSaker(final TilgangBruker tilgangBruker, final List<Tema> tema, final SafRequestContext safRequestContext) {
		try {
			if (tilgangBruker == null) {
				return Flowable.empty();
			}
			Flowable<List<Arkivsak>> gsakerFromOrgnr = Flowable.fromCallable(() ->
					gsakAntiCorruptionLayer.findArkivsakerByOrgnr(tilgangBruker.getOrgnummer(), tema))
					.subscribeOn(Schedulers.io());
			Flowable<List<Arkivsak>> gsakerFromAktoerId = Flowable.fromCallable(() ->
					gsakAntiCorruptionLayer.findArkivsakerByAktoerId(tilgangBruker.hentAlleAktoerId(), tema))
					.subscribeOn(Schedulers.io());
			Flowable<List<Arkivsak>> psaker = Flowable.fromCallable(() -> {
				if (!Collections.disjoint(tema, TEMA_PENSJON)) {
					return pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker, tema);
				} else {
					return new ArrayList<Arkivsak>();
				}
			}).subscribeOn(Schedulers.io());
			return Flowable.merge(Arrays.asList(gsakerFromOrgnr, gsakerFromAktoerId, psaker), 3)
					.flatMapIterable(items -> items)
					.map(arkivsak -> {
						final BidragSak bidragSak = getBidragSakIfTemaIsBidOrFar(arkivsak);
						safRequestContext.getRequestCache().putObject(arkivsak.getKey(), arkivsak);
						return TilgangSak.builder()
								.aktoerId(arkivsak.getAktoerId())
								.orgnummer(arkivsak.getOrgnummer())
								.tema(arkivsak.getTema())
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(arkivsak.getArkivsaksystem())
								.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
								.paragraf19(bidragSak == null ? null : bidragSak.isParagraf19())
								.build();
					});
		} catch (Exception e) {
			return Flowable.empty();
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
