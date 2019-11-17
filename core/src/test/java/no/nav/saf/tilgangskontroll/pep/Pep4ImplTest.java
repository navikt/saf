package no.nav.saf.tilgangskontroll.pep;

import static no.nav.abac.common.xacml.CommonAttributter.RESOURCE_FELLES_RESOURCE_TYPE;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_JOURNALSTATUS;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_JOURNAL_METADATA;
import static no.nav.abac.saf.xacml.SafAttributter.RESOURCE_SAF_SKJERMING;
import static no.nav.saf.domain.DomainConstants.ABAC_JOURNALSTATUS_UTGAAR;
import static no.nav.saf.domain.kode.Journalstatus.FERDIGSTILT;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlAttribute;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class Pep4ImplTest extends AbstractPepTest {

	@InjectMocks
	private Pep4Impl pep4;

	@Test
	void shouldPermitWhenJournalstatusNotUtgaarAndSkjermingIsNotPresent() {
		when(oidcValidatorTool.validate(AUTHORIZATION_HEADER)).thenReturn(true);

		boolean hasAccess = pep4.hasAccess(TilgangJournalpost.builder()
				.journalstatus(FERDIGSTILT)
				.skjerming(null)
				.build(), createSafRequestContext());

		assertTrue(hasAccess);
	}

	@Test
	void shouldPermitWhenJournalstatusUtgaarAndSaksbehandlerHasAccess() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		when(oidcValidatorTool.validate(AUTHORIZATION_HEADER)).thenReturn(true);

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep4.hasAccess(TilgangJournalpost.builder()
				.journalstatus(Journalstatus.UTGAAR)
				.build(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_JOURNAL_METADATA)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_SAF_JOURNALSTATUS, ABAC_JOURNALSTATUS_UTGAAR)));
	}

	@Test
	void shouldPermitWhenSkjermingIsPresentAndSaksbehandlerHasAccess() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		when(oidcValidatorTool.validate(AUTHORIZATION_HEADER)).thenReturn(true);

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep4.hasAccess(TilgangJournalpost.builder()
				.journalstatus(Journalstatus.FERDIGSTILT)
				.skjerming(SKJERMING_POL)
				.build(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_JOURNAL_METADATA)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_SAF_SKJERMING, SKJERMING_POL.name())));
	}

	@Test
	void shouldPermitWhenJournalstatusUtgaarAndSkjermingIsPresentAndSaksbehandlerHasAccess() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.PERMIT, null, null, null));
		when(oidcValidatorTool.validate(AUTHORIZATION_HEADER)).thenReturn(true);

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep4.hasAccess(TilgangJournalpost.builder()
				.journalstatus(Journalstatus.UTGAAR)
				.skjerming(SKJERMING_POL)
				.build(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertTrue(hasAccess);
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_JOURNAL_METADATA)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_SAF_JOURNALSTATUS, ABAC_JOURNALSTATUS_UTGAAR)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_SAF_SKJERMING, SKJERMING_POL.name())));
	}

	@Test
	void shouldDenyWhenJournalstatusUtgaarAndSkjermingIsPresentAndSaksbehandlerHasNotAccess() {
		when(abacService.evaluate(any(XacmlRequest.class))).thenReturn(new XacmlResponse(Decision.DENY, null, null, null));
		when(oidcValidatorTool.validate(AUTHORIZATION_HEADER)).thenReturn(true);

		ArgumentCaptor<XacmlRequest> request = ArgumentCaptor.forClass(XacmlRequest.class);

		boolean hasAccess = pep4.hasAccess(TilgangJournalpost.builder()
				.journalstatus(Journalstatus.UTGAAR)
				.skjerming(SKJERMING_POL)
				.build(), createSafRequestContext());

		verify(abacService).evaluate(request.capture());
		XacmlRequest capturedRequest = request.getValue();

		assertFalse(hasAccess);
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_FELLES_RESOURCE_TYPE, RESOURCE_SAF_JOURNAL_METADATA)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_SAF_JOURNALSTATUS, ABAC_JOURNALSTATUS_UTGAAR)));
		assertThat(capturedRequest.getResources(), hasItem(new XacmlAttribute(RESOURCE_SAF_SKJERMING, SKJERMING_POL.name())));
	}
}