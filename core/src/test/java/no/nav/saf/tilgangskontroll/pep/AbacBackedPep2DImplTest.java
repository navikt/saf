package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyConsumer;
import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyTematilgangResponse;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlAttribute;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.cache.support.NoOpCache;
import org.springframework.cache.support.SimpleCacheManager;

import java.util.Collections;
import java.util.Set;

import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.ARKIV;
import static no.nav.saf.anticorruptionlayer.joark.domain.kode.VariantFormatCode.ORIGINAL;
import static no.nav.saf.cache.ValkeyCacheConfig.VALKEY_DOKUMENT_TILGANG_CACHE;
import static no.nav.saf.domain.kode.Tema.FAR;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_TEMA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SAK_DOKUMENT;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbacBackedPep2DImplTest extends AbstractAbacBackedPepTest {

	private AbacBackedPep2dImpl pep2d;
	private AbacBackedPep2dImpl pep2dWithEntraProxy;

	@Mock
	private EntraProxyConsumer entraProxyConsumer;

	@BeforeEach
	void setUp() {
		super.setUp();
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Collections.singletonList(new NoOpCache(VALKEY_DOKUMENT_TILGANG_CACHE)));
		cacheManager.afterPropertiesSet();
		pep2d = new AbacBackedPep2dImpl(false, cacheManager, abacService, entraProxyConsumer);
		pep2dWithEntraProxy = new AbacBackedPep2dImpl(true, cacheManager, abacService, entraProxyConsumer);

	}

	@Test
	void shouldPermitWhenSystemAndVariantformatOriginal() {
		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.aktoerId(AKTOER_ID)
				.tema(TEMA_BID)
				.build(), createSafRequestContextSystem(ORIGINAL.name()));

		assertTrue(hasAccess);
	}

	@Test
	void shouldDenyWhenSystemAndVariantformatArkivWithoutTemaRole() {
		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.aktoerId(AKTOER_ID)
				.tema(TEMA_BID)
				.build(), createSafRequestContextSystem(ARKIV.name()));

		assertFalse(hasAccess);
	}

	@Test
	void shouldAllowWhenSystemAndVariantformatArkivWithTemaRole() {
		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.aktoerId(AKTOER_ID)
				.tema(TEMA_BID)
				.build(), createSafRequestContextSystemWithRoleBID(ARKIV.name()));

		assertFalse(hasAccess);
	}

	@Test
	void shouldDenyWhenSystemAndVariantformatArkivWithWrongTemaRole() {
		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.aktoerId(AKTOER_ID)
				.tema(TEMA_HJE)
				.build(), createSafRequestContextSystemWithRoleBID(ARKIV.name()));

		assertFalse(hasAccess);
	}

	@Test
	void shouldPermitWhenTemaIsAllowed() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.aktoerId(AKTOER_ID)
				.tema(TEMA_BID)
				.build(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertCommonXacmlRequestResources(capturedRequest);
	}

	private void assertCommonXacmlRequestResources(XacmlRequest capturedRequest) {
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_DOKUMENT)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_TEMA, TEMA_BID.name())));
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class, names = {"FAR", "KTA"})
	void shouldDenyWhenAbacDenies(Tema tema) {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.tema(tema)
				.build(), createSafRequestContext());

		assertFalse(hasAccess);
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class)
	void shouldPermitWhenFeatureToggleEntraProxyTrueAndHasTemaAccess(Tema tema) {
		when(entraProxyConsumer.hentTematilgangForNavAnsatt(any())).thenReturn(new EntraProxyTematilgangResponse(Set.of(tema.name())));

		boolean hasAccess = pep2dWithEntraProxy.hasAccess(TilgangSak.builder()
				.tema(tema)
				.build(), createSafRequestContext());

		assertTrue(hasAccess);
		verify(entraProxyConsumer).hentTematilgangForNavAnsatt(any());
		verify(abacService, never()).evaluate(any(XacmlRequest.class));
	}

	@Test
	void shouldDenyWhenFeatureToggleEntraProxyTrueAndDoesNotHaveTemaAccess() {
		when(entraProxyConsumer.hentTematilgangForNavAnsatt(any())).thenReturn(new EntraProxyTematilgangResponse(Set.of()));

		boolean hasAccess = pep2dWithEntraProxy.hasAccess(TilgangSak.builder()
				.tema(FAR)
				.build(), createSafRequestContext());

		assertFalse(hasAccess);
		verify(entraProxyConsumer).hentTematilgangForNavAnsatt(any());
		verify(abacService, never()).evaluate(any(XacmlRequest.class));
	}
}
