package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SAK_DOKUMENT;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_TEMA;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.saf.cache.RedisCacheConfig;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlAttribute;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.cache.support.NoOpCache;
import org.springframework.cache.support.SimpleCacheManager;

import java.util.Collections;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
class Pep2dImplTest extends AbstractPepTest {

	private Pep2dImpl pep2d;

	Pep2dImplTest() {
		super();
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Collections.singletonList(new NoOpCache(RedisCacheConfig.TILGANG_CACHE)));
		cacheManager.afterPropertiesSet();
		this.pep2d = new Pep2dImpl(cacheManager, abacService);
	}

	@Test
	void shouldPermitWhenTemaIsAllowed() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.aktoerId(AKTOER_ID)
				.tema(TEMA_BID)
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertCommonXacmlRequestResources(capturedRequest);
	}

	private void assertCommonXacmlRequestResources(XacmlRequest capturedRequest) {
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_DOKUMENT)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_SAF_TEMA, TEMA_BID.name())));
	}

	@Test
	void shouldDenyWhenAbacDenies() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);
		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.tema(Tema.FAR)
				.build(), new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		assertFalse(hasAccess);
	}
}
