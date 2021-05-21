package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.domain.tilgangsmodell.TilgangRelevantTredjepart;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlAttribute;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

import java.util.ArrayList;
import java.util.List;

import static no.nav.saf.domain.tilgangsmodell.IdentType.AKTOERID;
import static no.nav.saf.domain.tilgangsmodell.IdentType.FOLKEREGISTERIDENT;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_SAF_TREDJEPART;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Pep8ImplTest extends AbstractPepTest {

	@InjectMocks
	private Pep8Impl pep8;

	@Test
	void shouldPermit() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep8.hasAccess(createTilgangRelevatTredjepartList(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, AKTOER_ID)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_FNR, FNR)));
	}

	@Test
	void shouldPermitWhenNoResource() {
		boolean hasAccess = pep8.hasAccess(new ArrayList<>(), createSafRequestContext());
		verify(abacService, never()).evaluate(any());
		assertTrue(hasAccess);
	}

	@Test
	void shouldDenyWhenAbacDenies() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		boolean hasAccess = pep8.hasAccess(createTilgangRelevatTredjepartList(), createSafRequestContext());
		verify(abacService).evaluate(any());
		assertFalse(hasAccess);
	}

	private List<TilgangRelevantTredjepart> createTilgangRelevatTredjepartList() {
		List<TilgangRelevantTredjepart> tilgangRelevantTredjeparter = new ArrayList<>();
		tilgangRelevantTredjeparter.add(new TilgangRelevantTredjepart(TilgangIdent.builder().identType(FOLKEREGISTERIDENT).identifikator(FNR).build()));
		tilgangRelevantTredjeparter.add(new TilgangRelevantTredjepart(TilgangIdent.builder().identType(AKTOERID).identifikator(AKTOER_ID).build()));
		return tilgangRelevantTredjeparter;
	}
}
