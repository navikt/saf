package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_DOKUMENT_FIL;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SKJERMING;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.saf.cache.RedisCacheConfig;
import no.nav.saf.domain.kode.Variantformat;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
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
class Pep6dImplTest extends AbstractPepTest {

	private Pep6dImpl pep6d;

	Pep6dImplTest() {
		super();
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Collections.singletonList(new NoOpCache(RedisCacheConfig.TILGANG_CACHE)));
		cacheManager.afterPropertiesSet();
		this.pep6d = new Pep6dImpl(cacheManager, abacService);
	}

	@Test
	void shouldPermitWhenSkjermingIsNotPresent() {

		boolean hasAccess = pep6d.hasAccess(TilgangDokumentvariant.builder()
				.skjerming(null)
				.build(), createSafRequestContext());

		assertTrue(hasAccess);
	}

	@Test
	void shouldPermitWhenSkjermingIsPresentAndSaksbehandlerHasAccess() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep6d.hasAccess(TilgangDokumentvariant.builder()
				.skjerming(SKJERMING_POL)
				.variantformat(Variantformat.ARKIV)
				.build(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_DOKUMENT_FIL)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_SAF_SKJERMING, SKJERMING_POL.name())));
	}

	@Test
	void shouldDenyWhenSkjermingIsPresentAndSaksbehandlerHasNotAccess() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep6d.hasAccess(TilgangDokumentvariant.builder()
				.skjerming(SKJERMING_POL)
				.variantformat(Variantformat.ARKIV)
				.build(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertFalse(hasAccess);
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_DOKUMENT_FIL)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_SAF_SKJERMING, SKJERMING_POL.name())));
	}
}