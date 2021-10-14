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

import java.util.ArrayList;
import java.util.List;

import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_FORELDREPENGELOSNING;
import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_K9;
import static no.nav.saf.domain.kode.Tema.FOR;
import static no.nav.saf.domain.kode.Tema.FRI;
import static no.nav.saf.domain.kode.Tema.OMS;
import static no.nav.saf.tilgangskontroll.SafAttributter.RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE;
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

class Pep7ImplTest extends AbstractPepTest {

	@InjectMocks
	private Pep7Impl pep7;

	@Test
	void shouldPermitWhenNoAktoerId() {
		boolean hasAccess = pep7.hasAccess(TilgangSak.builder().build(), createSafRequestContext());
		verify(abacService, never()).evaluate(any());
		assertTrue(hasAccess);
	}

	@Test
	void shouldDenyWhenAbacDeniesForFagsaksystemFS36AndTemaFor() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		boolean hasAccess = pep7.hasAccess(createTilgangSakWithFpAktoerIdList(), createSafRequestContext());
		verify(abacService).evaluate(any());
		assertFalse(hasAccess);
	}

	@Test
	void shouldDenyWhenAbacDeniesForFagsaksystemK9AndTemaFri() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		boolean hasAccess = pep7.hasAccess(createTilgangSakWithK9AktoerIdList(FRI), createSafRequestContext());
		verify(abacService).evaluate(any());
		assertFalse(hasAccess);
	}

	@Test
	void shouldDenyWhenAbacDeniesForFagsaksystemK9AndTemaOms() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		boolean hasAccess = pep7.hasAccess(createTilgangSakWithK9AktoerIdList(OMS), createSafRequestContext());
		verify(abacService).evaluate(any());
		assertFalse(hasAccess);
	}

	@Test
	void shouldPermitWhenAbacPermitsForFagsakF36AndTemaFor() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep7.hasAccess(createTilgangSakWithFpAktoerIdList(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, FNR)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, FNR2)));
	}

	@Test
	void shouldPermitWhenAbacPermitsForFagsakK9AndTemaFri() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep7.hasAccess(createTilgangSakWithK9AktoerIdList(FRI), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, FNR)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, FNR2)));
	}

	@Test
	void shouldPermitWhenAbacPermitsForFagsakK9AndTemaOms() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep7.hasAccess(createTilgangSakWithK9AktoerIdList(OMS), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, FNR)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, FNR2)));
	}

	private TilgangSak createTilgangSakWithFpAktoerIdList() {
		List<String> aktoerIdList = new ArrayList<>();
		aktoerIdList.add(FNR);
		aktoerIdList.add(FNR2);
		return TilgangSak.builder()
				.fagsaksystem(FAGSAKSYSTEM_FORELDREPENGELOSNING)
				.tema(FOR)
				.fpAktoerIdList(aktoerIdList)
				.build();
	}

	private TilgangSak createTilgangSakWithK9AktoerIdList(Tema tema) {
		List<String> aktoerIdList = new ArrayList<>();
		aktoerIdList.add(FNR);
		aktoerIdList.add(FNR2);
		return TilgangSak.builder()
				.fagsaksystem(FAGSAKSYSTEM_K9)
				.tema(tema)
				.k9AktoerIdList(aktoerIdList)
				.build();
	}
}
