package no.nav.saf.tilgangskontroll.traverser;

import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.abstraction.UpwardsSecModelTraverser;
import no.nav.saf.tilgangskontroll.adapter.TilgangSakToTilgangBrukerParameterAdapter;
import no.nav.saf.tilgangskontroll.datafetcher.TilgangSakDataFetcher;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.stereotype.Component;

import javax.inject.Named;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class TilgangSakUpwardsSecModelTraverser extends UpwardsSecModelTraverser<TilgangSak> {

	public TilgangSakUpwardsSecModelTraverser(TilgangBrukerUpwardsSecModelTraverser tilgangBrukerUpwardsSecModelTraverser,
											  TilgangSakDataFetcher tilgangSakDataFetcher,
											  @Named("pep2") Pep<TilgangSak> pep2,
											  TilgangSakToTilgangBrukerParameterAdapter tilgangSakToTilgangBrukerParameterAdapter) {
		super(tilgangBrukerUpwardsSecModelTraverser, tilgangSakDataFetcher, pep2, tilgangSakToTilgangBrukerParameterAdapter);
	}
}
