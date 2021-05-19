package no.nav.saf.query.sak.repo;

import io.reactivex.Flowable;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tjeneste.argumenter.BrukerIdInput;

public interface SakBrukerTilgangsmodellRepository {


	TilgangBruker findTilgangBruker(BrukerIdInput brukerIdInput);

	Flowable<TilgangSak> findTilgangSaker(final TilgangBruker tilgangBruker, final SafRequestContext safRequestContext);

}
