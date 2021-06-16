package no.nav.saf.query.sak;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.domain.visningsmodell.Sak;
import no.nav.saf.metrics.Monitor;
import no.nav.saf.query.sak.repo.SakBrukerTilgangsmodellRepositoryImpl;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static no.nav.saf.domain.DomainConstants.TILGANG_BRUKER;
import static no.nav.saf.util.MDCUtility.addMdcData;

@Component
public class SakerCoordinatorImpl implements SakerCoordinator {

	private final Pep<TilgangBruker> pep1g;
	private final Pep<TilgangSak> pep2;
	private final Pep<TilgangSak> pep3;
	private final SakBrukerTilgangsmodellRepositoryImpl saksoversiktBrukerTilgangsmodellRepository;
	private final SakMapper sakMapper;


	@Inject
	public SakerCoordinatorImpl(Pep<TilgangBruker> pep1g,
								Pep<TilgangSak> pep2,
								Pep<TilgangSak> pep3,
								SakBrukerTilgangsmodellRepositoryImpl saksoversiktBrukerTilgangsmodellRepository,
								SakMapper sakermapper) {
		this.saksoversiktBrukerTilgangsmodellRepository = saksoversiktBrukerTilgangsmodellRepository;
		this.sakMapper = sakermapper;
		this.pep1g = pep1g;
		this.pep2 = pep2;
		this.pep3 = pep3;
	}

	@Override
	@Monitor(value = "dok_request", extraTags = {"process", "saker", "requestType", "bruker"}, histogram = true)
	public List<Sak> hentSaker(BrukerIdInput brukerIdInput, SafRequestContext safRequestContext) {
		TilgangBruker tilgangBruker = saksoversiktBrukerTilgangsmodellRepository.findTilgangBruker(brukerIdInput);
		if (tilgangBruker != null) {
			safRequestContext.getRequestCache().putObject(TILGANG_BRUKER, tilgangBruker);
		}

		boolean pep1gAccess = this.pep1g.hasAccess(tilgangBruker, safRequestContext);

		if (!pep1gAccess) {
			return Collections.emptyList();
		}

		final Flowable<TilgangSak> tilgangSakFlow = saksoversiktBrukerTilgangsmodellRepository.findTilgangSaker(tilgangBruker, safRequestContext);
		List<TilgangSak> filteredTilgangSakList = tilgangSakFlow
				.onErrorResumeNext(Flowable.empty())
				.parallel(10)
				.runOn(Schedulers.io())
				.doOnNext(ts -> addMdcData(safRequestContext))
				.filter(ts -> pep2.hasAccess(ts, safRequestContext))
				.filter(ts -> pep3.hasAccess(ts, safRequestContext))
				.sequential()
				.toList().blockingGet();

		return filteredTilgangSakList.stream()
				.map(tilgangSak ->
						sakMapper.mapSak(tilgangSak, safRequestContext.getRequestCache()))
				.filter(Objects::nonNull)
				.collect(Collectors.toSet())
				.stream().collect(Collectors.toList());
	}
}
