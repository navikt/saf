package no.nav.saf.tilgangskontroll.datafetcher;

import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.ARKIVSAKSNUMMER;
import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.ARKIVSAKSYSTEM;

import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.tilgangskontroll.abstraction.ParameterContext;
import no.nav.saf.tilgangskontroll.abstraction.SecModelDataFetcher;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Hans Petter Simonsen - Miles
 */
@Component
public class TilgangBrukerDataFetcher implements SecModelDataFetcher<TilgangBruker> {

	private final TilgangsmodellRepository tilgangsmodellRepository;

	public TilgangBrukerDataFetcher(TilgangsmodellRepository tilgangsmodellRepository) {
		this.tilgangsmodellRepository = tilgangsmodellRepository;
	}

	@Override
	public List<TilgangBruker> fetchAndFilter(ParameterContext parameterContext) {
		if (parameterContext.containsParameter(ARKIVSAKSNUMMER) && parameterContext.containsParameter(ARKIVSAKSYSTEM)) {
			return Arrays.asList(tilgangsmodellRepository.findTilgangBrukerBySakId(parameterContext.getParameter(ARKIVSAKSNUMMER), parameterContext.getParameter(ARKIVSAKSYSTEM)));
		} else {
			//TODO Støtt tilgangBruker fra jounalpost
			return new ArrayList<>();
		}

	}
}
