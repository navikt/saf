package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlAttribute;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

import java.util.Collections;

import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_PERSON;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
class Pep1gImplTest extends AbstractPepTest {

	@InjectMocks
	private Pep1gImpl pep1g;

	@Test
	void shouldPermitWhenAktoerIdIsEvaluated() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep1g.hasAccess(TilgangBruker.builder()
				.aktoerId(AKTOER_ID)
				.foedselsnr(FNR)
				.historiskeIdenter(Collections.singletonList(TilgangIdent.builder().identifikator(FNR).build()))
				.build(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertResourceType(capturedRequest);
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, AKTOER_ID)));
	}

	@Test
	void shouldPermitWhenFnrIsEvaluated() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep1g.hasAccess(TilgangBruker.builder()
				.foedselsnr(FNR)
				.historiskeIdenter(Collections.singletonList(TilgangIdent.builder().identifikator(FNR).build()))
				.build(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();
		assertResourceType(capturedRequest);
		assertEquals(FNR, capturedRequest.getResources().get(2).getValue().toString());
		assertTrue(hasAccess);
	}

	private void assertResourceType(XacmlRequest capturedRequest) {
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_PERSON)));
	}

	@Test
	void shouldDeny() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));

		boolean hasAccess = pep1g.hasAccess(TilgangBruker.builder()
				.aktoerId(AKTOER_ID)
				.foedselsnr(FNR)
				.historiskeIdenter(Collections.singletonList(TilgangIdent.builder().identifikator(FNR).build()))
				.build(), createSafRequestContext());

		assertFalse(hasAccess);
	}
}
