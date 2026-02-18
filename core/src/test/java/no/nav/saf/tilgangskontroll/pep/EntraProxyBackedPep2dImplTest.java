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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class EntraProxyBackedPep2dImplTest extends AbstractPepTest {

	private EntraProxyBackedPep2dImpl pep2d;

	@Mock
	private NavAnsattTemaService navAnsattTemaServiceMock;

	@BeforeEach
	void setUp() {
		super.setUp();
		pep2d = new EntraProxyBackedPep2dImpl(navAnsattTemaServiceMock);
	}

	@Test
	void shouldPermitWhenRessursIsNull() {
		var hasAccess = pep2d.hasAccess(null, createSafRequestContext());

		assertThat(hasAccess).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class)
	void shouldPermitWhenUserHasAccessToTema(Tema tema) {
		when(navAnsattTemaServiceMock.harTemaTilgang(any(SafRequestContext.class), eq(tema))).thenReturn(true);

		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.tema(tema)
				.build(), createSafRequestContext());

		assertThat(hasAccess).isTrue();
	}

	@Test
	void shouldDenyWhenUserDoesNotHaveAccessToTema() {
		when(navAnsattTemaServiceMock.harTemaTilgang(any(SafRequestContext.class), eq(FAR))).thenReturn(false);

		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.tema(FAR)
				.build(), createSafRequestContext());

		assertThat(hasAccess).isFalse();
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class)
	void shouldPermitForSystemUserWhenHasAccessToTema(Tema tema) {

		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.tema(tema)
				.build(), createSafRequestContextSystemUser(true, false));

		assertThat(hasAccess).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class)
	void shouldPermitForSystemUserWhenVariantformatIsOriginal(Tema tema) {

		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.tema(tema)
				.build(), createSafRequestContextSystemUser(false, true));

		assertThat(hasAccess).isTrue();
	}

	@Test
	void shouldDenyForSystemUserWhenDoesNotHaveAccessToTemaAndVariantformatIsNotOriginal() {

		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.tema(FAR)
				.build(), createSafRequestContextSystemUser(false, false));

		assertThat(hasAccess).isFalse();
	}

	private SafRequestContext createSafRequestContextSystemUser(boolean tilgangTilTema, boolean variantformatOriginal) {
		var safRequestContextMock = Mockito.mock(SafRequestContext.class);
		when(safRequestContextMock.getNavCallId()).thenReturn(NAV_CALLID);
		when(safRequestContextMock.getUserId()).thenReturn(null);
		when(safRequestContextMock.isUserIdNavAnsatt()).thenReturn(false);
		when(safRequestContextMock.isSystem()).thenReturn(true);
		when(safRequestContextMock.isSystemAndVariantformatOriginal()).thenReturn(variantformatOriginal);
		var safSecurityContextMock = Mockito.mock(SafSecurityContext.class);
		when(safSecurityContextMock.isJwtAzureClientCredentialFlow()).thenReturn(true);
		when(safRequestContextMock.isSystem()).thenReturn(true);
		when(safRequestContextMock.getSecurityContext()).thenReturn(safSecurityContextMock);
		when(safSecurityContextMock.hasDokumentTilgangEntraRole(any())).thenReturn(tilgangTilTema);
		var safRequestCache = Mockito.mock(RequestCache.class);
		when(safRequestContextMock.getRequestCache()).thenReturn(safRequestCache);
		return safRequestContextMock;
	}
}