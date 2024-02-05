package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlAttribute;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_TEMA;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_SAK_JP_METADATA;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Pep2ImplTest extends AbstractPepTest {

	@InjectMocks
	private Pep2Impl pep2;

	@Test
	void shouldPermitWhenTemaFarAndParagraf19AccessIsPermitted() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(Tema.FAR)
				.build(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_JP_METADATA)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_TEMA, Tema.FAR.name())));
	}

	@Test
	void shouldDenyWhenParagraf19IsNull() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(Tema.FAR)
				.build(), createSafRequestContext());

		assertFalse(hasAccess);
	}

	@Test
	void shouldDeny() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(Tema.FAR)
				.build(), createSafRequestContext());

		assertFalse(hasAccess);
	}
	@Test
	void shouldPermitWhenTemaKtaWhenEvaluateIsPermit() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(Tema.KTA)
				.build(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_SAK_JP_METADATA)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_TEMA, Tema.KTA.name())));
	}
	@Test
	void shouldDenyKTAWhenEvaluateIsFalse() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));

		boolean hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(Tema.KTA)
				.build(), createSafRequestContext());

		assertFalse(hasAccess);
	}
}
