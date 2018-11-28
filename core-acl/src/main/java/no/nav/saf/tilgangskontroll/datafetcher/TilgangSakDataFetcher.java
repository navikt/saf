package no.nav.saf.tilgangskontroll.datafetcher;

import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.abstraction.ParameterContext;
import no.nav.saf.tilgangskontroll.abstraction.SecModelDataFetcher;

import java.util.Arrays;
import java.util.List;

/**
 * @author Hans Petter Simonsen - Miles
 */

public class TilgangSakDataFetcher implements SecModelDataFetcher<TilgangSak> {

	private final TilgangsmodellRepository tilgangsmodellRepository;

	public TilgangSakDataFetcher(TilgangsmodellRepository tilgangsmodellRepository) {
		this.tilgangsmodellRepository = tilgangsmodellRepository;
	}

	@Override
	public List<TilgangSak> fetchAndFilter(ParameterContext parameterContext) {
		if (parameterContext.containsParameter("arkivsaksnummer") && parameterContext.containsParameter("arkivsaksystem")) {
			return Arrays.asList(tilgangsmodellRepository.findTilgangSakBySakId(parameterContext.getParameter("arkivsaksnummer"), parameterContext.getParameter("arkivsaksystem")));
		} else {
			//TODO Støtt tilgangBruker fra jounalpost
			return null;
		}

	}
}
