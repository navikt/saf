package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.SafSecurityContext;
import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.Decision;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static no.nav.saf.domain.kode.Journalstatus.FERDIGSTILT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class MsGraphBackedPep4ImplTest extends AbstractAbacBackedPepTest {

	private MsGraphBackedPep4Impl pep4;

	@Override
	SafRequestContext createSafRequestContext() {
		var safSecurityContextMock = Mockito.mock(SafSecurityContext.class);
		when(safSecurityContextMock.isJwtAzureClientCredentialFlow()).thenReturn(false);
		var safRequestContextMock = Mockito.mock(SafRequestContext.class);
		when(safRequestContextMock.getNavCallId()).thenReturn(NAV_CALLID);
		when(safRequestContextMock.getUserId()).thenReturn("Z123456");
		when(safRequestContextMock.isUserIdNavAnsatt()).thenReturn(true);
		when(safRequestContextMock.getSecurityContext()).thenReturn(safSecurityContextMock);
		return safRequestContextMock;
	}

	@BeforeEach
	void setUp() {
		super.setUp();
		pep4 = new MsGraphBackedPep4Impl(navUserGroupMembershipService);
	}

	@Test
	void shouldPermitWhenJournalstatusNotUtgaarAndSkjermingIsNotPresent() {
		boolean hasAccess = pep4.hasAccess(TilgangJournalpost.builder()
				.journalstatus(FERDIGSTILT)
				.skjerming(null)
				.build(), createSafRequestContext());

		assertTrue(hasAccess);
	}

	@Test
	void shouldPermitWhenJournalstatusUtgaarAndSaksbehandlerHasAccess() {
		when(navUserGroupMembershipService.isNavIdentInJoarkVedlikeholdGroup(anyString())).thenReturn(true);
		when(navUserGroupMembershipService.isNavIdentInLeseUtgaatteDokumenterGroup(anyString())).thenReturn(true);

		boolean hasAccess = pep4.hasAccess(TilgangJournalpost.builder()
				.journalstatus(Journalstatus.UTGAAR)
				.build(), createSafRequestContext());

		assertTrue(hasAccess);
	}

	@Test
	void shouldPermitWhenSkjermingIsPresentAndSaksbehandlerHasAccess() {
		when(navUserGroupMembershipService.isNavIdentInJoarkVedlikeholdGroup(anyString())).thenReturn(true);
		when(navUserGroupMembershipService.isNavIdentInLeseUtgaatteDokumenterGroup(anyString())).thenReturn(true);

		boolean hasAccess = pep4.hasAccess(TilgangJournalpost.builder()
				.journalstatus(Journalstatus.FERDIGSTILT)
				.skjerming(SKJERMING_POL)
				.build(), createSafRequestContext());

		assertTrue(hasAccess);
	}

	@Test
	void shouldPermitWhenJournalstatusUtgaarAndSkjermingIsPresentAndSaksbehandlerHasAccess() {
		when(navUserGroupMembershipService.isNavIdentInJoarkVedlikeholdGroup(anyString())).thenReturn(true);
		when(navUserGroupMembershipService.isNavIdentInLeseUtgaatteDokumenterGroup(anyString())).thenReturn(true);

		boolean hasAccess = pep4.hasAccess(TilgangJournalpost.builder()
				.journalstatus(Journalstatus.UTGAAR)
				.skjerming(SKJERMING_POL)
				.build(), createSafRequestContext());

		assertTrue(hasAccess);
	}

	@Test
	void shouldDenyWhenJournalstatusUtgaarAndSkjermingIsPresentAndSaksbehandlerHasNotAccess() {
		when(navUserGroupMembershipService.isNavIdentInJoarkVedlikeholdGroup(anyString())).thenReturn(false);
		when(navUserGroupMembershipService.isNavIdentInLeseUtgaatteDokumenterGroup(anyString())).thenReturn(false);

		boolean hasAccess = pep4.hasAccess(TilgangJournalpost.builder()
				.journalstatus(Journalstatus.UTGAAR)
				.skjerming(SKJERMING_POL)
				.build(), createSafRequestContext());

		assertFalse(hasAccess);
	}
}