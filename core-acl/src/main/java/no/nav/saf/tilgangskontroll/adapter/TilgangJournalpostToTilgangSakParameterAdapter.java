package no.nav.saf.tilgangskontroll.adapter;

import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.ARKIVSAKSNUMMER;
import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.ARKIVSAKSYSTEM;

import com.google.common.collect.Maps;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.tilgangskontroll.abstraction.ParameterContext;
import no.nav.saf.tilgangskontroll.abstraction.SecModelParameterAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Hans Petter Simonsen - Miles
 */
@Component
public class TilgangJournalpostToTilgangSakParameterAdapter implements SecModelParameterAdapter<TilgangJournalpost> {

	@Override
	public ParameterContext extractSearchParameter(TilgangJournalpost tilgangJournalpost) {
		Map<String, String> parameterMap = Maps.newHashMap();
		parameterMap.put(ARKIVSAKSNUMMER, tilgangJournalpost.getArkivsaksnummer());
		parameterMap.put(ARKIVSAKSYSTEM, tilgangJournalpost.getArkivsaksystem());
		return new ParameterContext(parameterMap);
	}
}
