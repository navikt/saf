package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.RequestCache;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.SafSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static no.nav.saf.tilgangskontroll.pep.PepAnswer.permit;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MsGraphBackedPep8DImplTest extends AbstractPepTest {

	private MsGraphBackedPep8dImpl pep8d;

	@BeforeEach
	void setUp() {
		super.setUp();
		pep8d = new MsGraphBackedPep8dImpl(navUserGroupMembershipService);
	}

	@Test
	void shouldPermitAndCacheWhenSakIsOpen() {
		when(navUserGroupMembershipService.isNavIdentInJoarkHistoriskGroup(anyString())).thenReturn(false);

		SafRequestContext safRequestContext = createSafRequestContext();
		boolean hasAccess = pep8d.hasAccess(TilgangSak.builder()
				.avsluttet(false)
				.tema(Tema.UKJ)
				.arkivsaksystem(Arkivsakssystem.GSAK)
				.arkivsaksnummer("123456")
				.build(), safRequestContext);

		assertTrue(hasAccess);
		verify(navUserGroupMembershipService, times(0)).isNavIdentInJoarkHistoriskGroup(anyString());
		verify(safRequestContext.getRequestCache(), times(1)).putDecision(any(), eq(permit()));
	}

	@Test
	void shouldPermitAndCacheWhenSakIsClosedAndUserMemberOfCorrectGroup() {
		when(navUserGroupMembershipService.isNavIdentInJoarkHistoriskGroup(anyString())).thenReturn(true);

		SafRequestContext safRequestContext = createSafRequestContext();
		boolean hasAccess = pep8d.hasAccess(TilgangSak.builder()
				.avsluttet(true)
				.tema(Tema.UKJ)
				.arkivsaksystem(Arkivsakssystem.GSAK)
				.arkivsaksnummer("123456")
				.build(), safRequestContext);

		assertTrue(hasAccess);
		verify(navUserGroupMembershipService, times(1)).isNavIdentInJoarkHistoriskGroup(anyString());
		verify(safRequestContext.getRequestCache(), times(1)).putDecision(any(), eq(permit()));
	}

	@Test
	void shouldDenyWhenSakIsClosedAndUserNotMemberOfCorrectGroup() {
		when(navUserGroupMembershipService.isNavIdentInJoarkHistoriskGroup(anyString())).thenReturn(false);

		SafRequestContext safRequestContext = createSafRequestContext();
		boolean hasAccess = pep8d.hasAccess(TilgangSak.builder()
				.avsluttet(true)
				.tema(Tema.UKJ)
				.arkivsaksystem(Arkivsakssystem.GSAK)
				.arkivsaksnummer("123456")
				.build(), safRequestContext);

		assertFalse(hasAccess);
		verify(navUserGroupMembershipService, times(1)).isNavIdentInJoarkHistoriskGroup(anyString());
		verify(safRequestContext.getRequestCache(), times(0)).putDecision(any(), eq(permit()));
	}

	@Test
	void shouldPermitAndCacheWhenSakIsClosedAndSystemUser() {
		SafRequestContext safRequestContextSystemUser = createSafRequestContextSystemUser();
		boolean hasAccess = pep8d.hasAccess(TilgangSak.builder()
				.avsluttet(true)
				.tema(Tema.UKJ)
				.arkivsaksystem(Arkivsakssystem.GSAK)
				.arkivsaksnummer("123456")
				.build(), safRequestContextSystemUser);

		assertTrue(hasAccess);
		verify(navUserGroupMembershipService, times(0)).isNavIdentInJoarkHistoriskGroup(anyString());
		verify(safRequestContextSystemUser.getRequestCache(), times(1)).putDecision(any(), eq(permit()));
	}

	@Test
	void shouldPermitAndCacheWhenSakIsOpenAndSystemUser() {
		SafRequestContext safRequestContextSystemUser = createSafRequestContextSystemUser();
		boolean hasAccess = pep8d.hasAccess(TilgangSak.builder()
				.avsluttet(false)
				.tema(Tema.UKJ)
				.arkivsaksystem(Arkivsakssystem.GSAK)
				.arkivsaksnummer("123456")
				.build(), safRequestContextSystemUser);

		assertTrue(hasAccess);
		verify(navUserGroupMembershipService, times(0)).isNavIdentInJoarkHistoriskGroup(anyString());
		verify(safRequestContextSystemUser.getRequestCache(), times(1)).putDecision(any(), eq(permit()));
	}


	@Override
	SafRequestContext createSafRequestContext() {
		var safRequestContextMock = Mockito.mock(SafRequestContext.class);
		when(safRequestContextMock.getNavCallId()).thenReturn(NAV_CALLID);
		when(safRequestContextMock.getUserId()).thenReturn("Z123456");
		when(safRequestContextMock.isUserIdNavAnsatt()).thenReturn(true);
		when(safRequestContextMock.isSystem()).thenReturn(false);
		var safSecurityContextMock = Mockito.mock(SafSecurityContext.class);
		when(safSecurityContextMock.isJwtAzureClientCredentialFlow()).thenReturn(false);
		when(safRequestContextMock.getSecurityContext()).thenReturn(safSecurityContextMock);
		var safRequestCache = Mockito.mock(RequestCache.class);
		when(safRequestContextMock.getRequestCache()).thenReturn(safRequestCache);
		return safRequestContextMock;
	}


	SafRequestContext createSafRequestContextSystemUser() {
		var safRequestContextMock = Mockito.mock(SafRequestContext.class);
		when(safRequestContextMock.getNavCallId()).thenReturn(NAV_CALLID);
		when(safRequestContextMock.getUserId()).thenReturn(null);
		when(safRequestContextMock.isUserIdNavAnsatt()).thenReturn(false);
		when(safRequestContextMock.isSystem()).thenReturn(true);
		var safSecurityContextMock = Mockito.mock(SafSecurityContext.class);
		when(safSecurityContextMock.isJwtAzureClientCredentialFlow()).thenReturn(true);
		when(safSecurityContextMock.isSystem()).thenReturn(true);
		when(safRequestContextMock.getSecurityContext()).thenReturn(safSecurityContextMock);
		var safRequestCache = Mockito.mock(RequestCache.class);
		when(safRequestContextMock.getRequestCache()).thenReturn(safRequestCache);
		return safRequestContextMock;
	}


}
