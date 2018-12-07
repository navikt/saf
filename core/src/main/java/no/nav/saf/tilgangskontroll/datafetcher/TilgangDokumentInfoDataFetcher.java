package no.nav.saf.tilgangskontroll.datafetcher;

import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.DOKUMENTINFO_ID;
import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.JOURNALPOST_ID;
import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.VARIANTFORMAT;

import no.nav.saf.domain.TilgangsmodellRepository;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
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
public class TilgangDokumentInfoDataFetcher implements SecModelDataFetcher<TilgangDokumentInfo> {

	private final TilgangsmodellRepository tilgangsmodellRepository;

	public TilgangDokumentInfoDataFetcher(TilgangsmodellRepository tilgangsmodellRepository) {
		this.tilgangsmodellRepository = tilgangsmodellRepository;
	}

	@Override
	public List<TilgangDokumentInfo> fetchAndFilter(ParameterContext parameterContext) {
		if (parameterContext.containsParameter(JOURNALPOST_ID) && parameterContext.containsParameter(DOKUMENTINFO_ID)
				&& parameterContext.containsParameter(VARIANTFORMAT)) {
			return Arrays.asList(tilgangsmodellRepository.findTilgangDokumentInfo(parameterContext.getParameter(JOURNALPOST_ID),
					parameterContext.getParameter(DOKUMENTINFO_ID), parameterContext.getParameter(VARIANTFORMAT)));
		} else {
			return new ArrayList<>();
		}
	}
}
