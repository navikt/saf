package no.nav.saf.query.dokumentoversikt.bruker;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.bisys.BisysAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.fpsak.FpsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.gsak.GsakAntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.k9.K9AntiCorruptionLayer;
import no.nav.saf.anticorruptionlayer.pensjonsak.PensjonSakAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.BidragSak;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
class DokumentoversiktBrukerTilgangsmodellRepository {
	private static final Set<Tema> TEMA_PENSJON = EnumSet.of(Tema.PEN, Tema.UFO);

	private final GsakAntiCorruptionLayer gsakAntiCorruptionLayer;
	private final PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer;
	private final BisysAntiCorruptionLayer bisysAntiCorruptionLayer;
	private final FpsakAntiCorruptionLayer fpsakAntiCorruptionLayer;
	private final K9AntiCorruptionLayer k9AntiCorruptionLayer;

	@Autowired
	public DokumentoversiktBrukerTilgangsmodellRepository(
			GsakAntiCorruptionLayer gsakAntiCorruptionLayer,
			PensjonSakAntiCorruptionLayer pensjonSakAntiCorruptionLayer,
			BisysAntiCorruptionLayer bisysAntiCorruptionLayer,
			FpsakAntiCorruptionLayer fpsakAntiCorruptionLayer,
			K9AntiCorruptionLayer k9AntiCorruptionLayer
	) {
		this.gsakAntiCorruptionLayer = gsakAntiCorruptionLayer;
		this.pensjonSakAntiCorruptionLayer = pensjonSakAntiCorruptionLayer;
		this.bisysAntiCorruptionLayer = bisysAntiCorruptionLayer;
		this.fpsakAntiCorruptionLayer = fpsakAntiCorruptionLayer;
		this.k9AntiCorruptionLayer = k9AntiCorruptionLayer;
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
							gsakAntiCorruptionLayer.findArkivsakerByAktoerId(tilgangBruker.getAlleAktoerIds(), tema))
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
						final BidragSak bidragSak = bisysAntiCorruptionLayer.hentBidragSakByArkivsak(arkivsak);
						List<String> fpsak = fpsakAntiCorruptionLayer.hentRelevanteParter(arkivsak);
						List<String> k9sak = k9AntiCorruptionLayer.hentRelevanteParter(arkivsak);
						safRequestContext.getRequestCache().putArkivsak(arkivsak);
						return TilgangSak.builder()
								.aktoerId(arkivsak.getAktoerId())
								.orgnummer(arkivsak.getOrgnummer())
								.tema(arkivsak.getTema())
								.arkivsaksnummer(arkivsak.getArkivsaksnummer())
								.arkivsaksystem(arkivsak.getArkivsaksystem())
								.fagsaksystem(arkivsak.getFagsaksystem())
								.relevanteTredjeparter(bidragSak == null ? null : new ArrayList<>(bidragSak.getRelevanteTredjeparter()))
								.fpAktoerIdList(fpsak)
								.k9AktoerIdList(k9sak)
								.build();
					});
		} catch (Exception e) {
			return Flowable.empty();
		}
	}
}
