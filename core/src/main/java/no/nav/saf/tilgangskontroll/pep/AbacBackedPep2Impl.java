package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyConsumer;
import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyTematilgangResponse;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import no.nav.saf.tilgangskontroll.pep.reasons.TemaReason;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static no.nav.saf.domain.DomainConstants.PEP2;
import static no.nav.saf.domain.kode.Tema.FAR;
import static no.nav.saf.domain.kode.Tema.KTA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_TEMA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SAK_JP_METADATA;
import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;

/**
 * Dekker følgende policies i saf:
 * <p>
 * https://confluence.adeo.no/display/ABAC/Tilgang+til+farskapssaker
 */
@Slf4j
@Component(PEP2)
public class AbacBackedPep2Impl extends StandardAbacBackedPep<TilgangSak> {

	private final boolean featureToggleEntraProxy;
	private final AbacService abacService;
	private final EntraProxyConsumer entraProxyConsumer;

	@Autowired
	public AbacBackedPep2Impl(@Value("${saf.pep2.feature_toggle_entra_proxy}") boolean featureToggleEntraProxy,
							  AbacService abacService,
							  EntraProxyConsumer entraProxyConsumer) {
		this.featureToggleEntraProxy = featureToggleEntraProxy;
		this.abacService = abacService;
		this.entraProxyConsumer = entraProxyConsumer;
	}

	@Override
	public PepAnswer verifyAbacPdpDecision(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.error("Pep2 (tema FAR eller KTA) mangler data om journalposten. Den må ha tema for å gjøre tilgangskontroll. Dette er forårsaket av en teknisk feil");
			return PepAnswer.deny(new UkjentEllerTekniskReason());
		}

		Tema tema = ressurs.getTema();

		if (isFarskap(tema) || isKontrollAnmeldelse(tema)) {

			if (featureToggleEntraProxy) {
				try {
					EntraProxyTematilgangResponse response = entraProxyConsumer.hentTematilgangForNavAnsatt(safRequestContext);

					if (response.harTilgangTilTema(tema)) {
						return PepAnswer.permit();
					}

					return getDenyAnswerForTema(tema);
				} catch (Exception e) {
					log.error("Pep2 (tema FAR eller KTA): Kall mot Entra-proxy feilet, fallback til abac-saf.", e);
				}
			}

			XacmlRequest request = SafXacmlRequestFactory.create(safRequestContext.getSecurityContext());
			request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_JP_METADATA);
			request.resource(RESOURCE_FELLES_TEMA, tema.name());

			traceLogPepStarted(PEP2, ressurs);
			XacmlResponse response = abacService.evaluate(request);
			traceLogPepFinished(PEP2, ressurs);

			return response.isPermit() ? PepAnswer.permit() : PepAnswer.deny(new TemaReason(response.getAdvicesMap(), tema));

		} else {
			return PepAnswer.permit();
		}
	}

	@Override
	protected PepAnswer translateToDenyReasonCode(XacmlResponse xacmlResponse) {
		return null;
	}

	@Override
	public PepAnswer verifyAzureClientCredentialFlowAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.error("Pep2 (tema FAR eller KTA) mangler data om journalposten. Den må ha tema for å gjøre tilgangskontroll. Dette er forårsaket av en teknisk feil");
			return PepAnswer.deny(new TemaReason(
					"cause_0013_ikketilgangtiltema", "saf_pep2", "mangler_tema", null
			));
		}
		Tema tema = ressurs.getTema();

		if (isFarskap(tema) || isKontrollAnmeldelse(tema)) {
			return safRequestContext.getSecurityContext().hasJournalTilgangEntraRole(tema) ?
					permit() : getDenyAnswerForTema(tema);
		} else {
			return permit();
		}
	}

	private boolean isFarskap(Tema tema) {
		return FAR.equals(tema);
	}

	private boolean isKontrollAnmeldelse(Tema tema) {
		return KTA.equals(tema);
	}

	private PepAnswer getDenyAnswerForTema(Tema tema) {
		String policy = switch (tema) {
			case FAR -> "saf_farskap";
			case KTA -> "saf_kontrollanmeldelse";
			default -> "saf_pep2";
		};

		return PepAnswer.deny(new TemaReason(
				"cause_0013_ikketilgangtilJournaltema",
				policy,
				"tematilgang_nok",
				tema));
	}

}
