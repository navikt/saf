package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.anticorruptionlayer.nav.NavAnsattTemaService;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.RequestCache;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.SafSecurityContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.Mockito;

import static no.nav.saf.domain.kode.Tema.FAR;
import static no.nav.saf.domain.kode.Tema.KTA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class EntraProxyBackedPep2ImplTest extends AbstractPepTest {

	private EntraProxyBackedPep2Impl pep2;

	@Mock
	private NavAnsattTemaService navAnsattTemaServiceMock;

	@BeforeEach
	void setUp() {
		super.setUp();
		pep2 = new EntraProxyBackedPep2Impl(navAnsattTemaServiceMock);
	}

	@Test
	void shouldDenyWhenRessursIsNull() {
		var hasAccess = pep2.hasAccess(null, createSafRequestContext());

		assertThat(hasAccess).isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class, names = {"FAR", "KTA"})
	void shouldPermitForRelevantTema(Tema tema) {
		when(navAnsattTemaServiceMock.harTemaTilgang(any(SafRequestContext.class), eq(tema))).thenReturn(true);

		var hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(tema)
				.build(), createSafRequestContext());

		assertThat(hasAccess).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class, names = {"FAR", "KTA"})
	void shouldDenyForRelevantTemaWhenUserDoesNotHaveAccess(Tema tema) {
		when(navAnsattTemaServiceMock.harTemaTilgang(any(SafRequestContext.class), eq(tema))).thenReturn(false);

		var hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(tema)
				.build(), createSafRequestContext());

		assertThat(hasAccess).isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class, names = {"FAR", "KTA"}, mode = EXCLUDE)
	void shouldPermitForAllNotRelevantTema(Tema tema) {

		var hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(tema)
				.build(), createSafRequestContext());

		assertThat(hasAccess).isTrue();
	}

	@Test
	void shouldDeny() {
		when(navAnsattTemaServiceMock.harTemaTilgang(any(SafRequestContext.class), eq(KTA))).thenReturn(false);

		var hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(FAR)
				.build(), createSafRequestContext());

		assertThat(hasAccess).isFalse();
	}

	@Test
	void shouldDenyForSystemUserWhenRessursIsNull() {
		var hasAccess = pep2.hasAccess(null, createSafRequestContextSystemUser(true));

		assertThat(hasAccess).isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class, names = {"FAR", "KTA"})
	void shouldPermitForRelevantTemaWhenSystemUserHasAccessToTema(Tema tema) {
		var hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(tema)
				.build(), createSafRequestContextSystemUser(true));

		assertThat(hasAccess).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class, names = {"FAR", "KTA"})
	void shouldDenyForRelevantTemaWhenSystemUserDoesNotHaveAccessToTema(Tema tema) {
		var hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(tema)
				.build(), createSafRequestContextSystemUser(false));

		assertThat(hasAccess).isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class, names = {"FAR", "KTA"}, mode = EXCLUDE)
	void shouldPermitForAllNotRelevantTemaWhenSystemUser(Tema tema) {
		var hasAccess = pep2.hasAccess(TilgangSak.builder()
				.tema(tema)
				.build(), createSafRequestContextSystemUser(true));

		assertThat(hasAccess).isTrue();
	}

	private SafRequestContext createSafRequestContextSystemUser(boolean tilgangTilTema) {
		var safRequestContextMock = Mockito.mock(SafRequestContext.class);
		when(safRequestContextMock.getNavCallId()).thenReturn(NAV_CALLID);
		when(safRequestContextMock.getUserId()).thenReturn(null);
		when(safRequestContextMock.isUserIdNavAnsatt()).thenReturn(false);
		when(safRequestContextMock.isSystem()).thenReturn(true);
		var safSecurityContextMock = Mockito.mock(SafSecurityContext.class);
		when(safSecurityContextMock.isSystem()).thenReturn(true);
		when(safSecurityContextMock.isJwtAzureClientCredentialFlow()).thenReturn(true);
		when(safRequestContextMock.getSecurityContext()).thenReturn(safSecurityContextMock);
		when(safSecurityContextMock.hasJournalTilgangEntraRole(any())).thenReturn(tilgangTilTema);
		var safRequestCache = Mockito.mock(RequestCache.class);
		when(safRequestContextMock.getRequestCache()).thenReturn(safRequestCache);
		return safRequestContextMock;
	}
}