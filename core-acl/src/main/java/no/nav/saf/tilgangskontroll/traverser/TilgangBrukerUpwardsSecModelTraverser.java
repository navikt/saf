package no.nav.saf.tilgangskontroll.traverser;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.abstraction.UpwardsSecModelTraverser;
import no.nav.saf.tilgangskontroll.datafetcher.TilgangBrukerDataFetcher;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.stereotype.Component;

import javax.inject.Named;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class TilgangBrukerUpwardsSecModelTraverser extends UpwardsSecModelTraverser<TilgangBruker> {

	public TilgangBrukerUpwardsSecModelTraverser(TilgangBrukerDataFetcher tilgangBrukerDataFetcher,
												 @Named("pep1") Pep<TilgangBruker> pep1) {
		super(null, tilgangBrukerDataFetcher, pep1, null);
	}
}
