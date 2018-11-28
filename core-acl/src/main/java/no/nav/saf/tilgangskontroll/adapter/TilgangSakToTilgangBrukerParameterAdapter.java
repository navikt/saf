package no.nav.saf.tilgangskontroll.adapter;

import com.google.common.collect.Maps;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.abstraction.ParameterContext;
import no.nav.saf.tilgangskontroll.abstraction.SecModelParameterAdapter;

import java.util.Map;

/**
 * @author Hans Petter Simonsen - Miles
 */
public class TilgangSakToTilgangBrukerParameterAdapter implements SecModelParameterAdapter<TilgangSak> {

	@Override
	public ParameterContext extractSearchParameter(TilgangSak tilgangSak) {
		Map<String, String> parameterMap = Maps.newHashMap();
		parameterMap.put("arkivsaksnummer", tilgangSak.getArkivsaksnummer());
		parameterMap.put("arkivsaksystem", tilgangSak.getArkivsaksystem());
		return new ParameterContext(parameterMap);
	}
}
