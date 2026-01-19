package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyConsumer;
import no.nav.saf.anticorruptionlayer.nav.entraproxy.EntraProxyTematilgangResponse;
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
import org.springframework.cache.support.NoOpCache;
import org.springframework.cache.support.SimpleCacheManager;

import java.util.Collections;
import java.util.Set;

import static no.nav.saf.cache.ValkeyCacheConfig.VALKEY_DOKUMENT_TILGANG_CACHE;
import static no.nav.saf.domain.kode.Tema.FAR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EntraProxyBackedPep2dImplTest extends AbstractPepTest {

	private EntraProxyBackedPep2dImpl pep2d;

	@Mock
	private EntraProxyConsumer entraProxyConsumer;

	@BeforeEach
	void setUp() {
		super.setUp();
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Collections.singletonList(new NoOpCache(VALKEY_DOKUMENT_TILGANG_CACHE)));
		cacheManager.afterPropertiesSet();
		pep2d = new EntraProxyBackedPep2dImpl(cacheManager, entraProxyConsumer);
	}

	@Test
	void shouldPermitWhenRessursIsNull() {
		var hasAccess = pep2d.hasAccess(null, createSafRequestContext());

		assertThat(hasAccess).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class)
	void shouldPermitWhenUserHasAccessToTema(Tema tema) {
		when(entraProxyConsumer.hentTematilgangForNavAnsatt(any())).thenReturn(new EntraProxyTematilgangResponse(Set.of(tema.name())));

		boolean hasAccess = pep2d.hasAccess(TilgangSak.builder()
				.tema(tema)
				.build(), createSafRequestContext());

		assertThat(hasAccess).isTrue();
	}

	@Test
	void shouldDenyWhenUserDoesNotHaveAccessToTema() {
		when(entraProxyConsumer.hentTematilgangForNavAnsatt(any())).thenReturn(new EntraProxyTematilgangResponse(Set.of()));

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
		when(safRequestContextMock.isSystemAndVariantformatOriginal()).thenReturn(variantformatOriginal);
		var safSecurityContextMock = Mockito.mock(SafSecurityContext.class);
		when(safSecurityContextMock.isJwtAzureClientCredentialFlow()).thenReturn(true);
		when(safRequestContextMock.getSecurityContext()).thenReturn(safSecurityContextMock);
		when(safSecurityContextMock.hasDokumentTilgangEntraRole(any())).thenReturn(tilgangTilTema);
		var safRequestCache = Mockito.mock(RequestCache.class);
		when(safRequestContextMock.getRequestCache()).thenReturn(safRequestCache);
		return safRequestContextMock;
	}
}