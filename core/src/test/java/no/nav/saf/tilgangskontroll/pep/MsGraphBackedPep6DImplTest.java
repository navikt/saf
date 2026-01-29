package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.kode.Variantformat;
import no.nav.saf.domain.tilgangsmodell.TilgangDokumentvariant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MsGraphBackedPep6DImplTest extends AbstractPepTest {

	private MsGraphBackedPep6dImpl pep6d;

	@BeforeEach
	void setUp() {
		super.setUp();
		this.pep6d = new MsGraphBackedPep6dImpl(navUserGroupMembershipService);
	}

	@Test
	void shouldPermitWhenSkjermingIsNotPresent() {

		boolean hasAccess = pep6d.hasAccess(TilgangDokumentvariant.builder()
				.skjerming(null)
				.build(), createSafRequestContext());

		verify(navUserGroupMembershipService, times(0)).isNavIdentInJoarkVedlikeholdGroup(anyString());

		assertTrue(hasAccess);
	}

	@Test
	void shouldPermitWhenSkjermingIsPresentAndSaksbehandlerHasAccess() {
		when(navUserGroupMembershipService.isNavIdentInJoarkVedlikeholdGroup(anyString())).thenReturn(true);

		boolean hasAccess = pep6d.hasAccess(TilgangDokumentvariant.builder()
				.skjerming(SKJERMING_POL)
				.variantformat(Variantformat.ARKIV)
				.build(), createSafRequestContext());

		verify(navUserGroupMembershipService, times(1)).isNavIdentInJoarkVedlikeholdGroup(anyString());
		assertTrue(hasAccess);
	}

	@Test
	void shouldDenyWhenSkjermingIsPresentAndSaksbehandlerHasNotAccess() {
		when(navUserGroupMembershipService.isNavIdentInJoarkVedlikeholdGroup(anyString())).thenReturn(false);

		boolean hasAccess = pep6d.hasAccess(TilgangDokumentvariant.builder()
				.skjerming(SKJERMING_POL)
				.variantformat(Variantformat.ARKIV)
				.build(), createSafRequestContext());

		verify(navUserGroupMembershipService, times(1)).isNavIdentInJoarkVedlikeholdGroup(anyString());
		assertFalse(hasAccess);
	}
}