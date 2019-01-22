package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_PERSON_FNR;
import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_TREDJEPART;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.domain.tilgangsmodell.TilgangRelevantTredjepart;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlAttribute;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
public class Pep3ImplTest extends AbstractPepTest {

	@InjectMocks
	private Pep3Impl pep3;

	@Test
	public void shouldPermitWhenTemaIsFarAndRelevanteTredjeparterSupplied() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep3.hasAccess(createTilgangSakBuilderWithTemaBidAndRelevanteTredjeparter().tema(Tema.FAR.name())
						.build(),
				new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_FNR, FNR)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_FNR, FNR2)));
	}

	@Test
	public void shouldPermitWhenTemaIsBidAndRelevanteTredjeparterSupplied() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);
		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep3.hasAccess(createTilgangSakBuilderWithTemaBidAndRelevanteTredjeparter().build(),
				new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);

		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_TREDJEPART)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_FNR, FNR)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_PERSON_FNR, FNR2)));
	}


	@Test
	public void shouldPermitWhenTemaIsNotFarOrBidAndRelevanteTredjeparterSupplied() {
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);

		boolean hasAccess = pep3.hasAccess(createTilgangSakBuilderWithTemaBidAndRelevanteTredjeparter().tema(Tema.PEN.name())
						.build(),
				new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		verify(abacService, never()).evaluate(any());
		assertTrue(hasAccess);
	}

	@Test
	public void shouldDenyWhenTemaIsBidOrFarButRelevanteTredjeparterIsNotSupplied() {
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);

		boolean hasAccess = pep3.hasAccess(createTilgangSakBuilderWithTemaBidAndRelevanteTredjeparter().relevanteTredjeparter(new ArrayList<>())
						.build(),
				new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));

		verify(abacService, never()).evaluate(any());
		assertFalse(hasAccess);
	}

	@Test
	public void shouldDenyWhenAbacDenies() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		when(oidcValidatorTool.validate(OIDC_TOKEN_PERSON_USER_TEST)).thenReturn(true);
		boolean hasAccess = pep3.hasAccess(createTilgangSakBuilderWithTemaBidAndRelevanteTredjeparter().build(),
				new SafRequestContext(OIDC_TOKEN_PERSON_USER_TEST, oidcValidatorTool));
		verify(abacService).evaluate(any());
		assertFalse(hasAccess);
	}

	private TilgangSak.TilgangSakBuilder createTilgangSakBuilderWithTemaBidAndRelevanteTredjeparter() {
		return TilgangSak.builder()
				.tema(TEMA_BID)
				.relevanteTredjeparter(Arrays.asList(new TilgangRelevantTredjepart(TilgangIdent.builder()
								.identifikator(FNR)
								.build()),
						new TilgangRelevantTredjepart(TilgangIdent.builder()
								.identifikator(FNR2)
								.build())));
	}
}
