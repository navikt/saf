package no.nav.saf.tilgangskontroll.model;

import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abstraction.ParameterContext;
import no.nav.saf.tilgangskontroll.abstraction.SecModelWorld;
import no.nav.saf.tilgangskontroll.traverser.TilgangDokumentInfoUpwardsSecModelTraverser;
import org.springframework.stereotype.Component;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class HentDokumentTilgangsmodell {

	private final TilgangDokumentInfoUpwardsSecModelTraverser tilgangDokumentInfoUpwardsSecModelTraverser;

	public HentDokumentTilgangsmodell(TilgangDokumentInfoUpwardsSecModelTraverser tilgangDokumentInfoUpwardsSecModelTraverser) {
		this.tilgangDokumentInfoUpwardsSecModelTraverser = tilgangDokumentInfoUpwardsSecModelTraverser;
	}

	public TilgangDokumentInfo checkTilgangDokumentInfo(SafRequestContext safRequestContext,
														ParameterContext parameterContext,
														SecModelWorld secModelWorld) {
		return tilgangDokumentInfoUpwardsSecModelTraverser.fetchAndFilterAndEnforce(parameterContext, safRequestContext, secModelWorld)
				.stream()
				.findAny()
				.orElse(null);
	}

}
