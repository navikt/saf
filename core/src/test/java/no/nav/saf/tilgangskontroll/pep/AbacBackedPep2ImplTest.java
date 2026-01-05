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
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Set;

import static no.nav.saf.domain.kode.Tema.FAR;
import static no.nav.saf.domain.kode.Tema.KTA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_TEMA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SAK_JP_METADATA;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AbacBackedPep2ImplTest extends AbstractAbacBackedPepTest {

	private AbacBackedPep2Impl pep2;
	private AbacBackedPep2Impl pep2WithEntraProxy;

	@Mock
	private EntraProxyConsumer entraProxyConsumer;

	@BeforeEach
	void setUp() {
		super.setUp();
		pep2 = new AbacBackedPep2Impl(false, abacService, entraProxyConsumer);
		pep2WithEntraProxy = new AbacBackedPep2Impl(true, abacService, entraProxyConsumer);
	}

	@Test
	void shouldPermitWhenTemaFarAndParagraf19AccessIsPermitted() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(FAR)
				.build(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_JP_METADATA)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_TEMA, FAR.name())));
	}

	@Test
	void shouldDenyWhenParagraf19IsNull() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(FAR)
				.build(), createSafRequestContext());

		assertFalse(hasAccess);
	}

	@Test
	void shouldDeny() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(FAR)
				.build(), createSafRequestContext());

		assertFalse(hasAccess);
	}
	@Test
	void shouldPermitWhenTemaKtaWhenEvaluateIsPermit() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(KTA)
				.build(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_JP_METADATA)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_TEMA, KTA.name())));
	}
	@Test
	void shouldDenyKTAWhenEvaluateIsFalse() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(KTA)
				.build(), createSafRequestContext());

		assertFalse(hasAccess);
	}

	@ParameterizedTest
	@ValueSource(strings = {"FAR", "KTA"})
	void shouldPermitWhenFeatureToggleEntraProxyTrueAndHasTemaAccess(String tema) {
		when(entraProxyConsumer.hentTematilgangForNavAnsatt(any())).thenReturn(new EntraProxyTematilgangResponse(Set.of(tema)));

		boolean hasAccess = pep2WithEntraProxy.hasAccess(TilgangSak.builder()
				.tema(Tema.valueOf(tema))
				.build(), createSafRequestContext());

		assertTrue(hasAccess);
		verify(entraProxyConsumer).hentTematilgangForNavAnsatt(any());
		verify(abacService, never()).evaluate(any(XacmlRequest.class));
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class, names = {"FAR", "KTA"}, mode = EnumSource.Mode.EXCLUDE)
	void shouldPermitAndNotCallEntraProxyWhenFeatureToggleEntraProxyTrueAndTemaNotFARorKTA(Tema tema) {
		when(entraProxyConsumer.hentTematilgangForNavAnsatt(any())).thenReturn(new EntraProxyTematilgangResponse(Set.of(tema.name())));

		boolean hasAccess = pep2WithEntraProxy.hasAccess(TilgangSak.builder()
				.tema(tema)
				.build(), createSafRequestContext());

		assertTrue(hasAccess);
		verify(entraProxyConsumer, never()).hentTematilgangForNavAnsatt(any());
		verify(abacService, never()).evaluate(any(XacmlRequest.class));
	}

	@Test
	void shouldDenyWhenFeatureToggleEntraProxyTrueAndDoesNotHaveTemaAccess() {
		when(entraProxyConsumer.hentTematilgangForNavAnsatt(any())).thenReturn(new EntraProxyTematilgangResponse(Set.of()));

		boolean hasAccess = pep2WithEntraProxy.hasAccess(TilgangSak.builder()
				.tema(FAR)
				.build(), createSafRequestContext());

		assertFalse(hasAccess);
		verify(entraProxyConsumer).hentTematilgangForNavAnsatt(any());
		verify(abacService, never()).evaluate(any(XacmlRequest.class));
	}
}
