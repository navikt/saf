package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlAttribute;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

import static java.util.Arrays.asList;
import static no.nav.saf.cache.KeyGeneratorLocalCaching.getKeyForPep7d;
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

class Pep7dImplTest extends AbstractPepTest {

	public static final String ARKIVSAKSNUMMER = "100000000";

	@InjectMocks
	private Pep7dImpl pep7d;

	@Test
	void shouldPermitWhenNoAktoerId() {
		TilgangSak tilgangSak = TilgangSak.builder()
				.arkivsaksystem(Arkivsakssystem.GSAK)
				.arkivsaksnummer("100000000")
				.tema(FOR)
				.build();
		boolean hasAccess = pep7d.hasAccess(tilgangSak, createSafRequestContext());
		verify(abacService, never()).evaluate(any());
		assertTrue(hasAccess);
	}

	@Test
	void shouldPermitWhenNoArkivsaksystemOrArkivsaknummer() { // Midlertidig journalført uten sakstilknytning
		TilgangSak tilgangSak = TilgangSak.builder()
				.arkivsaksystem(null)
				.arkivsaksnummer(null)
				.tema(FOR)
				.build();
		boolean hasAccess = pep7d.hasAccess(tilgangSak, createSafRequestContext());
		verify(abacService, never()).evaluate(any());
		assertTrue(hasAccess);
	}

	@Test
	void shouldDenyWhenAbacDeniesForFagsaksystemFS36AndTemaFor() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		SafRequestContext safRequestContext = createSafRequestContext();
		boolean hasAccess = pep7d.hasAccess(createTilgangSakWithFpAktoerIdList(), safRequestContext);
		verify(abacService).evaluate(any());
		assertFalse(hasAccess);
		assertFalse(((AbacAnswer) safRequestContext.getRequestCache().getObject(getKeyForPep7d(Arkivsakssystem.GSAK, ARKIVSAKSNUMMER))).isPermit());
	}

	@Test
	void shouldDenyWhenAbacDeniesForFagsaksystemK9AndTemaFri() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		SafRequestContext safRequestContext = createSafRequestContext();
		boolean hasAccess = pep7d.hasAccess(createTilgangSakWithK9AktoerIdList(FRI), safRequestContext);
		verify(abacService).evaluate(any());
		assertFalse(hasAccess);
		assertFalse(((AbacAnswer) safRequestContext.getRequestCache().getObject(getKeyForPep7d(Arkivsakssystem.GSAK, ARKIVSAKSNUMMER))).isPermit());
	}

	@Test
	void shouldDenyWhenAbacDeniesForFagsaksystemK9AndTemaOms() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		SafRequestContext safRequestContext = createSafRequestContext();
		boolean hasAccess = pep7d.hasAccess(createTilgangSakWithK9AktoerIdList(OMS), safRequestContext);
		verify(abacService).evaluate(any());
		assertFalse(hasAccess);
		assertFalse(((AbacAnswer) safRequestContext.getRequestCache().getObject(getKeyForPep7d(Arkivsakssystem.GSAK, ARKIVSAKSNUMMER))).isPermit());
	}

	@Test
	void shouldPermitWhenAbacPermitsForFagsakF36AndTemaFor() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		SafRequestContext safRequestContext = createSafRequestContext();
		boolean hasAccess = pep7d.hasAccess(createTilgangSakWithFpAktoerIdList(), safRequestContext);

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertTrue(((AbacAnswer) safRequestContext.getRequestCache().getObject(getKeyForPep7d(Arkivsakssystem.GSAK, ARKIVSAKSNUMMER))).isPermit());
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, FNR)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, FNR2)));
	}

	@Test
	void shouldPermitWhenAbacPermitsForFagsakK9AndTemaFri() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		SafRequestContext safRequestContext = createSafRequestContext();
		boolean hasAccess = pep7d.hasAccess(createTilgangSakWithK9AktoerIdList(FRI), safRequestContext);

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);
		assertTrue(((AbacAnswer) safRequestContext.getRequestCache().getObject(getKeyForPep7d(Arkivsakssystem.GSAK, ARKIVSAKSNUMMER))).isPermit());
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, FNR)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, FNR2)));
	}

	@Test
	void shouldPermitWhenAbacPermitsForFagsakK9AndTemaOms() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		SafRequestContext safRequestContext = createSafRequestContext();
		boolean hasAccess = pep7d.hasAccess(createTilgangSakWithK9AktoerIdList(OMS), safRequestContext);

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);
		assertTrue(((AbacAnswer) safRequestContext.getRequestCache().getObject(getKeyForPep7d(Arkivsakssystem.GSAK, ARKIVSAKSNUMMER))).isPermit());
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, FNR)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_AKTOERID_RESOURCE, FNR2)));
	}

	private TilgangSak createTilgangSakWithFpAktoerIdList() {
		return TilgangSak.builder()
				.fagsaksystem(FAGSAKSYSTEM_FORELDREPENGELOSNING)
				.tema(FOR)
				.fpAktoerIdList(asList(FNR, FNR2))
				.arkivsaksystem(Arkivsakssystem.GSAK)
				.arkivsaksnummer(ARKIVSAKSNUMMER)
				.build();
	}

	private TilgangSak createTilgangSakWithK9AktoerIdList(Tema tema) {
		return TilgangSak.builder()
				.fagsaksystem(FAGSAKSYSTEM_K9)
				.tema(tema)
				.k9AktoerIdList(asList(FNR, FNR2))
				.arkivsaksystem(Arkivsakssystem.GSAK)
				.arkivsaksnummer(ARKIVSAKSNUMMER)
				.build();
	}
}
