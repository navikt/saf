package no.nav.saf.tilgangskontroll.adapter;

import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.ARKIVSAKSNUMMER;
import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.ARKIVSAKSYSTEM;

import com.google.common.collect.Maps;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.abstraction.ParameterContext;
import no.nav.saf.tilgangskontroll.abstraction.SecModelParameterAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Hans Petter Simonsen - Miles
 */
@Component
public class TilgangSakToTilgangBrukerParameterAdapter implements SecModelParameterAdapter<TilgangSak> {

	@Override
	public ParameterContext extractSearchParameter(TilgangSak tilgangSak) {
		Map<String, String> parameterMap = Maps.newHashMap();
		parameterMap.put(ARKIVSAKSNUMMER, tilgangSak.getArkivsaksnummer());
		parameterMap.put(ARKIVSAKSYSTEM, tilgangSak.getArkivsaksystem());
		return new ParameterContext(parameterMap);
	}
}
