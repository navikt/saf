package no.nav.saf.tilgangskontroll.adapter;

import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.DOKUMENTINFO_ID;
import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.JOURNALPOST_ID;
import static no.nav.saf.tilgangskontroll.abstraction.ParameterConstants.VARIANTFORMAT;

import com.google.common.collect.Maps;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.tilgangskontroll.abstraction.ParameterContext;
import no.nav.saf.tilgangskontroll.abstraction.SecModelParameterAdapter;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Hans Petter Simonsen - Miles
 */
@Component
public class TilgangDokumentInfoToTilgangJournalpostParameterAdapter implements SecModelParameterAdapter<TilgangDokumentInfo> {

	@Override
	public ParameterContext extractSearchParameter(TilgangDokumentInfo tilgangDokumentInfo) {
		Map<String, String> parameterMap = Maps.newHashMap();
		parameterMap.put(JOURNALPOST_ID, tilgangDokumentInfo.getJournalpostId());
		parameterMap.put(DOKUMENTINFO_ID, tilgangDokumentInfo.getDokumentInfoId());
		parameterMap.put(VARIANTFORMAT, tilgangDokumentInfo.getVariantFormat());
		return new ParameterContext(parameterMap);
	}
}
