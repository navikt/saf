package no.nav.saf.tilgangskontroll.datafetcher;

import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.DOKUMENTINFO_ID;
import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.JOURNALPOST_ID;
import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.VARIANTFORMAT;

import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
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
public class TilgangJournalpostDataFetcher implements SecModelDataFetcher<TilgangJournalpost> {

	private final TilgangsmodellRepository tilgangsmodellRepository;

	public TilgangJournalpostDataFetcher(TilgangsmodellRepository tilgangsmodellRepository) {
		this.tilgangsmodellRepository = tilgangsmodellRepository;
	}

	@Override
	public List<TilgangJournalpost> fetchAndFilter(ParameterContext parameterContext) {
		if (parameterContext.containsParameter(JOURNALPOST_ID) && parameterContext.containsParameter(DOKUMENTINFO_ID)
				&& parameterContext.containsParameter(VARIANTFORMAT)) {
			return Arrays.asList(tilgangsmodellRepository.findTilgangJournalpost(parameterContext.getParameter(JOURNALPOST_ID),
					parameterContext.getParameter(DOKUMENTINFO_ID), parameterContext.getParameter(VARIANTFORMAT)));
		} else {
			return new ArrayList<>();
		}
	}
}
