package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_OIDC_TOKEN_BODY;
import static no.nav.abac.common.xacml.CommonAttributter.ENVIRONMENT_FELLES_PEP_ID;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_DOMENE;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_JOURNALPOST;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_TEMA;
import static no.nav.saf.cache.RedisCacheConfig.TILGANG_CACHE;
import static no.nav.saf.domain.DomainConstants.SAF;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.RedisCacheConfig;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import no.nav.saf.tilgangskontroll.abac.service.AbacService;
import no.nav.saf.tjeneste.visningsmodell.kode.Tema;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Slf4j
@Component("pep2")
public class Pep2Impl implements Pep<TilgangSak> {

	private final Cache tilgangCache;
	private final AbacService abacService;

	@Inject
	public Pep2Impl(@Named(RedisCacheConfig.MANAGER_DISTRIBUTED) CacheManager redisCacheManager, AbacService abacService) {
		this.tilgangCache = redisCacheManager.getCache(TILGANG_CACHE);
		this.abacService = abacService;
	}

	@Override
	public boolean hasAccess(TilgangSak ressurs, SafRequestContext safRequestContext) {
		if (ressurs == null) {
			log.warn("Pep2 mangler tilstrekkelig datagrunnlag for å kunne gjennomføre tilgangskontroll");
			return false;
		}

		if (Tema.FAR.name().equals(ressurs.getTema())) {
			if (log.isTraceEnabled()) {
				log.trace("Pep2 evaluerer arkivsak={}, arkivsaksystem={}, tema={}", ressurs.getArkivsaksnummer(), ressurs.getArkivsaksystem(), ressurs.getTema());
			}
			XacmlRequest request = new XacmlRequest();

			request.environment(ENVIRONMENT_FELLES_OIDC_TOKEN_BODY, safRequestContext.getSecurityContext().getOidcTokenBody());
			request.environment(ENVIRONMENT_FELLES_PEP_ID, SAF);
			request.resource(RESOURCE_FELLES_DOMENE, SAF);
			request.resource(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_JOURNALPOST);
			request.resource(RESOURCE_SAF_TEMA, Tema.FAR.name());
			XacmlResponse response = abacService.evaluate(request);
			// TODO distributed cache
			if (log.isTraceEnabled()) {
				log.trace("Pep2 ferdig evaluert arkivsak={}, arkivsaksystem={}, tema={}", ressurs.getArkivsaksnummer(), ressurs.getArkivsaksystem(), ressurs.getTema());
			}
			return Decision.PERMIT.equals(response.getDecision());
		} else {
			return true;
		}
	}
}
