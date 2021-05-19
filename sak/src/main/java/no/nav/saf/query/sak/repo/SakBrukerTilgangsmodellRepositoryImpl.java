package no.nav.saf.query.sak.repo;

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
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
@Slf4j
public class SakBrukerTilgangsmodellRepositoryImpl implements SakBrukerTilgangsmodellRepository {

	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final PdlAntiCorruptionLayer aktoerAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;

	@Inject
	public SakBrukerTilgangsmodellRepositoryImpl(GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
												 PdlAntiCorruptionLayer aktoerAntiCorruptionLayer,
												 BisysAntiCorruptionLayer bisysAntiCorruptionLayer,
												 PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer) {
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.aktoerAntiCorruptionLayer = aktoerAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
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

	public Flowable<TilgangSak> findTilgangSaker(final TilgangBruker tilgangBruker, final SafRequestContext safRequestContext) {
		try {
			if (tilgangBruker == null) {
				return Flowable.empty();
			}
			Flowable<List<Arkivsak>> gsakerFromOrgnr = Flowable.fromCallable(() ->
					gsakAntiCorruptionLayer.findArkivsakerByOrgnr(tilgangBruker.getOrgnummer()))
					.subscribeOn(Schedulers.io());
			Flowable<List<Arkivsak>> gsakerFromAktoerId = Flowable.fromCallable(() ->
					gsakAntiCorruptionLayer.findArkivsakerByAktoerId(tilgangBruker.getAktoerId()))
					.subscribeOn(Schedulers.io());
			Flowable<List<Arkivsak>> psaker = Flowable.fromCallable(() ->
					pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker))
			.subscribeOn(Schedulers.io());

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
