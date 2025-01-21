package no.nav.saf.query.sak;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import no.nav.saf.anticorruptionlayer.aktoer.PdlAntiCorruptionLayer;
import no.nav.saf.domain.Arkivsak;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.query.sak.repo.SakBrukerTilgangsmodellRepository;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsLast;
import static java.util.stream.Collectors.groupingBy;
import static no.nav.saf.util.MDCUtility.addMdcData;

@Component
class SakerQuery {

	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep3;
	private final SakBrukerTilgangsmodellRepository sakBrukerTilgangsmodellRepository;
	private final PdlAntiCorruptionLayer aktoerAntiCorruptionLayer;
	private final SakMapper sakMapper;

	@Autowired
	public SakerQuery(Pep<TilgangBruker> pep1g,
					  Pep<TilgangSak> pep2,
					  Pep<TilgangSak> pep3,
					  SakBrukerTilgangsmodellRepository sakBrukerTilgangsmodellRepository, PdlAntiCorruptionLayer aktoerAntiCorruptionLayer,
					  SakMapper sakermapper) {
		this.sakBrukerTilgangsmodellRepository = sakBrukerTilgangsmodellRepository;
		this.aktoerAntiCorruptionLayer = aktoerAntiCorruptionLayer;
		this.sakMapper = sakermapper;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep3 = pep3;
	}

	@Monitor(value = "dok_request", extraTags = {"process", "saker", "requestType", "bruker"}, histogram = true)
	public List<Sak> hentSaker(BrukerIdInput brukerIdInput, SafRequestContext safRequestContext) {
		TilgangBruker tilgangBruker = aktoerAntiCorruptionLayer.findTilgangBruker(brukerIdInput);
		if (tilgangBruker != null) {
			safRequestContext.getRequestCache().putTilgangBruker(tilgangBruker);
		}

		boolean pep1gAccess = this.pep1g.hasAccess(tilgangBruker, safRequestContext);

		if (!pep1gAccess) {
			return Collections.emptyList();
		}

		Map<String, Arkivsak> arkivsakMap = new HashMap<>();
		final Flowable<TilgangSak> tilgangSakFlow = sakBrukerTilgangsmodellRepository.findTilgangSaker(tilgangBruker, arkivsakMap);
		List<TilgangSak> filteredTilgangSakList = tilgangSakFlow
				.onErrorResumeWith(Flowable.empty())
				.parallel(10)
				.runOn(Schedulers.io())
				.doOnNext(ts -> addMdcData(safRequestContext))
				.filter(ts -> pep2.hasAccess(ts, safRequestContext))
				.filter(ts -> pep3.hasAccess(ts, safRequestContext))
				.sequential()
				.toList().blockingGet();

		var distinctSaker = filteredTilgangSakList.stream()
				.map(tilgangSak ->
						sakMapper.mapSak(arkivsakMap.get(tilgangSak.getCacheKey())))
				.filter(Objects::nonNull)
				.distinct()
				.toList();

		return distinctByArkivsaksnummerAndTemaAndArkivsaksystem(distinctSaker);
	}

	private List<Sak> distinctByArkivsaksnummerAndTemaAndArkivsaksystem(List<Sak> saker) {
		return saker.stream()
				.collect(groupingBy(sak -> asList(getSakKey(sak), sak.getTema())))
				.values()
				.stream()
				.map(this::filterByArkivsaksystem)
				.flatMap(this::getOldestByDatoOpprettet)
				.sorted(nullsLast(comparing(Sak::getArkivsaksnummer)))
				.toList();
	}

	private static String getSakKey(Sak sak) {
		return sak.getFagsakId() != null ? sak.getFagsakId() : sak.getArkivsaksnummer();
	}

	private List<Sak> filterByArkivsaksystem(List<Sak> saker) {
		var psaker = saker.stream()
				.filter(Sak::isPsak)
				.toList();

		return psaker.isEmpty() ? saker : psaker;
	}

	private Stream<Sak> getOldestByDatoOpprettet(List<Sak> saker) {
		return saker.stream()
				.min(nullsLast(comparing(Sak::getDatoOpprettet)))
				.stream();
	}
}
