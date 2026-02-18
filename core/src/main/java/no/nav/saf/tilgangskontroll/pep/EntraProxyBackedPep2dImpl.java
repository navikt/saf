package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.NavAnsattTemaService;
import no.nav.saf.cache.KeyGeneratorLocalCaching;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.reasons.TemaReason;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.PEP2D;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/pages/viewpage.action?pageId=313329243
 * <p>
 * Lokal caching er kun relevant for dokumentoversiktene og brukes i journalpostMapperDto.java
 */
@Slf4j
@Component(PEP2D)
public class EntraProxyBackedPep2dImpl extends StandardEntraProxyBackedPep<TilgangSak> {

	private static final String MANGLER_RESSURS_MELDING = "Pep2d (tema-tilgang) mangler data om sak. Tilgang gis likevel for at {} skal kunne knytte dokument til sak og bruker.";

	private final NavAnsattTemaService navAnsattTemaService;

	public EntraProxyBackedPep2dImpl(NavAnsattTemaService navAnsattTemaService) {
		this.navAnsattTemaService = navAnsattTemaService;
	}

	@Override
	PepAnswer verifyNavIdentAccessToTema(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.info(MANGLER_RESSURS_MELDING, "saksbehandler");
			return permit();
		}

		traceLogPepStarted(PEP2D, ressurs);
		Tema tema = ressurs.getTema();
		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(tema);

		PepAnswer pepAnswer;
		// Try-catch er fordi redis ikke fungerer lokalt
		pepAnswer = harNavAnsattTemaTilgang(ressurs, safRequestContext);
		safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
		traceLogPepFinished(PEP2D, ressurs);
		return pepAnswer;
	}

	@Override
	public PepAnswer verifyAccessForSystem(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.info(MANGLER_RESSURS_MELDING, "system");
			return permit();
		}

		traceLogPepStarted(PEP2D, ressurs);
		Tema tema = ressurs.getTema();
		String tilgangKeyLocalCaching = KeyGeneratorLocalCaching.getKeyForPep2d(tema);

		boolean decision = safRequestContext.isSystemAndVariantformatOriginal() || safRequestContext.getSecurityContext().hasDokumentTilgangEntraRole(tema);

		PepAnswer pepAnswer = decision ? permit() : PepAnswer.deny(new TemaReason(
				"cause_0013_ikketilgangtilDokumenttema", "saf_pep2d", "mangler_tema", tema));

		safRequestContext.getRequestCache().putDecision(tilgangKeyLocalCaching, pepAnswer);
		traceLogPepFinished(PEP2D, ressurs);

		return pepAnswer;
	}

	private PepAnswer harNavAnsattTemaTilgang(TilgangSak ressurs, SafRequestContext safRequestContext) {
		Tema tema = ressurs.getTema();
		boolean harTemaTilgang = navAnsattTemaService.harTemaTilgang(safRequestContext, tema);

		if (harTemaTilgang) {
			return permit();
		}

		TemaReason temaReason = new TemaReason("cause_0013_ikketilgangtilDokumenttema", "saf_pep2d", "tematilgang_nok", tema);
		return PepAnswer.deny(temaReason);
	}
}
