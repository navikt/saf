package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.anticorruptionlayer.nav.NavOrgService;
import no.nav.saf.anticorruptionlayer.nav.TilgangsmaskinenConsumer;
import no.nav.saf.domain.tilgangsmodell.TilgangBruker;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.integration.token.NaisTexasConsumer;
import no.nav.saf.integration.token.OboToken;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.SafSecurityContext;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TilgangsmaskinenBackedPep1gImplTest extends AbstractAbacBackedPepTest {

	private static final String ORGNUMMER = "123465987";
	@Mock
	NavOrgService navOrgService;
	@Mock
	NaisTexasConsumer naisTexasConsumer;
	@Mock
	TilgangsmaskinenConsumer tilgangsmaskinenConsumer;

	private TilgangsmaskinenBackedPep1gImpl pep1g;

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
		pep1g = new TilgangsmaskinenBackedPep1gImpl(naisTexasConsumer, tilgangsmaskinenConsumer, navOrgService, navUserGroupMembershipService);
		when(naisTexasConsumer.exchangeForTilgangsmaskinenOboToken(any(), any())).thenReturn("Yeeehaaaw!");
	}

	@Test
	void shouldPermitWhenAktoerIdIsEvaluated() {
		when(tilgangsmaskinenConsumer.navIdentHasAccess(any(), any())).thenReturn(PepAnswer.permit());

		boolean hasAccess = pep1g.hasAccess(TilgangBruker.builder()
				.aktoerId(AKTOER_ID)
				.foedselsnr(FNR)
				.historiskeIdenter(Collections.singletonList(TilgangIdent.builder().identifikator(FNR).build()))
				.build(), createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(1)).navIdentHasAccess(any(), any());
		verify(navOrgService, times(0)).isOrganisasjonsnummerNavBedrift(any());
		verify(navUserGroupMembershipService, times(0)).isNavIdentInEgenAnsattGroup(any());

		assertTrue(hasAccess);
	}

	@Test
	void shouldPermitWhenFnrIsEvaluated() {
		when(tilgangsmaskinenConsumer.navIdentHasAccess(any(), any())).thenReturn(PepAnswer.permit());

		boolean hasAccess = pep1g.hasAccess(TilgangBruker.builder()
				.foedselsnr(FNR)
				.historiskeIdenter(Collections.singletonList(TilgangIdent.builder().identifikator(FNR).build()))
				.build(), createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(1)).navIdentHasAccess(any(), any());
		verify(navOrgService, times(0)).isOrganisasjonsnummerNavBedrift(any());
		verify(navUserGroupMembershipService, times(0)).isNavIdentInEgenAnsattGroup(any());

		assertTrue(hasAccess);
	}

	@Test
	void shouldPermitWhenOrgNrIsEvaluated() {
		when(navOrgService.isOrganisasjonsnummerNavBedrift(any())).thenReturn(false);

		boolean hasAccess = pep1g.hasAccess(TilgangBruker.builder()
				.orgnummer(ORGNUMMER)
				.build(), createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(0)).navIdentHasAccess(any(), any());
		verify(navOrgService, times(1)).isOrganisasjonsnummerNavBedrift(any());
		verify(navUserGroupMembershipService, times(0)).isNavIdentInEgenAnsattGroup(any());

		assertTrue(hasAccess);
	}

	@Test
	void shouldDenyWhenOrgNrIsEvaluatedAndNotEgenAnsattAndNavOrg() {
		when(navOrgService.isOrganisasjonsnummerNavBedrift(any())).thenReturn(true);
		when(navUserGroupMembershipService.isNavIdentInEgenAnsattGroup(any())).thenReturn(false);

		boolean hasAccess = pep1g.hasAccess(TilgangBruker.builder()
				.orgnummer(ORGNUMMER)
				.build(), createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(0)).navIdentHasAccess(any(), any());
		verify(navOrgService, times(1)).isOrganisasjonsnummerNavBedrift(any());
		verify(navUserGroupMembershipService, times(1)).isNavIdentInEgenAnsattGroup(any());

		assertFalse(hasAccess);
	}

	@Test
	void shouldPermitWhenOrgNrIsEvaluatedAndEgenAnsattAndNavOrg() {
		when(navOrgService.isOrganisasjonsnummerNavBedrift(any())).thenReturn(true);
		when(navUserGroupMembershipService.isNavIdentInEgenAnsattGroup(any())).thenReturn(true);

		boolean hasAccess = pep1g.hasAccess(TilgangBruker.builder()
				.orgnummer(ORGNUMMER)
				.build(), createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(0)).navIdentHasAccess(any(), any());
		verify(navOrgService, times(1)).isOrganisasjonsnummerNavBedrift(any());
		verify(navUserGroupMembershipService, times(1)).isNavIdentInEgenAnsattGroup(any());

		assertTrue(hasAccess);
	}

	@Test
	void shouldDeny() {
		when(tilgangsmaskinenConsumer.navIdentHasAccess(any(), any())).thenReturn(PepAnswer.deny(new UkjentEllerTekniskReason()));

		boolean hasAccess = pep1g.hasAccess(TilgangBruker.builder()
				.aktoerId(AKTOER_ID)
				.foedselsnr(FNR)
				.historiskeIdenter(Collections.singletonList(TilgangIdent.builder().identifikator(FNR).build()))
				.build(), createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(1)).navIdentHasAccess(any(), any());
		verify(navOrgService, times(0)).isOrganisasjonsnummerNavBedrift(any());
		verify(navUserGroupMembershipService, times(0)).isNavIdentInEgenAnsattGroup(any());

		assertFalse(hasAccess);
	}
}