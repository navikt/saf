package no.nav.saf.query.sak.repo;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.sak.SakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class SakBrukerTilgangsmodellRepository {

	private final SakAntiCorruptionLayer sakAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;

	@Autowired
	public SakBrukerTilgangsmodellRepository(SakAntiCorruptionLayer sakAntiCorruptionLayer,
											 BisysAntiCorruptionLayer bisysAntiCorruptionLayer,
											 PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer) {
		this.sakAntiCorruptionLayer = sakAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
	}

	public Flowable<TilgangSak> findTilgangSaker(final TilgangBruker tilgangBruker, Map<String, Arkivsak> arkivsakMap) {
		try {
			if (tilgangBruker == null) {
				return Flowable.empty();
			}
			Flowable<List<Arkivsak>> gsakerFromOrgnr = Flowable.fromCallable(() ->
							sakAntiCorruptionLayer.findArkivsakerByOrgnr(tilgangBruker.getOrgnummer()))
					.subscribeOn(Schedulers.io());
			Flowable<List<Arkivsak>> gsakerFromAktoerId = Flowable.fromCallable(() ->
							sakAntiCorruptionLayer.findArkivsakerByAktoerId(tilgangBruker.getAktoerId()))
					.subscribeOn(Schedulers.io());
			Flowable<List<Arkivsak>> psaker = Flowable.fromCallable(() ->
							pensjonSakAntiCorruptionLayer.findArkivsaker(tilgangBruker))
					.subscribeOn(Schedulers.io());

			return Flowable.merge(Arrays.asList(gsakerFromOrgnr, gsakerFromAktoerId, psaker), 3)
					.flatMapIterable(items -> items)
					.map(arkivsak -> {
						arkivsakMap.put(arkivsak.getKey(), arkivsak);
						final BidragSak bidragSak = bisysAntiCorruptionLayer.hentBidragSakByArkivsak(arkivsak);
						return TilgangSak.builder()
								.aktoerId(arkivsak.getAktoerId())
								.orgnummer(arkivsak.getOrgnummer())
								.tema(arkivsak.getTema())
								.avsluttet(arkivsak.isAvsluttet())
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(arkivsak.getArkivsaksystem())
								.fagsaksystem(arkivsak.getFagsaksystem())
								.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
								.build();
					});
		} catch (Exception e) {
			return Flowable.empty();
		}
	}
}
