package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MsGraphBackedPep5ImplTest extends AbstractPepTest {

	@InjectMocks
	private MsGraphBackedPep5Impl pep5;

	@BeforeEach
	void setUp() {
		super.setUp();
		pep5 = new MsGraphBackedPep5Impl(navUserGroupMembershipService);
	}

	@Test
	void shouldPermitWhenSkjermingIsNotPresent() {
		when(navUserGroupMembershipService.isNavIdentInJoarkVedlikeholdGroup(anyString())).thenReturn(false);

		boolean hasAccess = pep5.hasAccess(TilgangDokumentInfo.builder()
				.skjerming(null)
				.build(), createSafRequestContext());

		verify(navUserGroupMembershipService, times(0)).isNavIdentInJoarkVedlikeholdGroup(anyString());
		assertTrue(hasAccess);
	}

	@Test
	void shouldPermitWhenSkjermingIsPresentAndSaksbehandlerHasAccess() {
		when(navUserGroupMembershipService.isNavIdentInJoarkVedlikeholdGroup(anyString())).thenReturn(true);

		boolean hasAccess = pep5.hasAccess(TilgangDokumentInfo.builder()
				.skjerming(SKJERMING_POL)
				.build(), createSafRequestContext());

		assertTrue(hasAccess);
		verify(navUserGroupMembershipService, times(1)).isNavIdentInJoarkVedlikeholdGroup(anyString());
	}

	@Test
	void shouldDenyWhenSkjermingIsPresentAndSaksbehandlerHasNotAccess() {
		when(navUserGroupMembershipService.isNavIdentInJoarkVedlikeholdGroup(anyString())).thenReturn(false);

		boolean hasAccess = pep5.hasAccess(TilgangDokumentInfo.builder()
				.skjerming(SKJERMING_POL)
				.build(), createSafRequestContext());

		assertFalse(hasAccess);
		verify(navUserGroupMembershipService, times(1)).isNavIdentInJoarkVedlikeholdGroup(anyString());
	}

}
