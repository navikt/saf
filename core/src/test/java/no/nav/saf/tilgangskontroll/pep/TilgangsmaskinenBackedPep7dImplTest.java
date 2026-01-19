package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenConsumer;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.RequestCache;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.SafSecurityContext;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Stream;

import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_FORELDREPENGELOSNING;
import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_K9;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TilgangsmaskinenBackedPep7dImplTest extends AbstractPepTest {

	private static final String IDENTIFIKATOR = "12345678910";
	private static final String IDENTIFIKATOR_2 = "01987654321";
	private static final String ARKIVSAKSNUMMER = "100000000";

	@Mock
	TilgangsmaskinenConsumer tilgangsmaskinenConsumer;

	private TilgangsmaskinenBackedPep7dImpl pep7d;

	@Override
	SafRequestContext createSafRequestContext() {
		var safSecurityContextMock = Mockito.mock(SafSecurityContext.class);
		when(safSecurityContextMock.isJwtAzureClientCredentialFlow()).thenReturn(false);
		var safRequestContextMock = Mockito.mock(SafRequestContext.class);
		when(safRequestContextMock.getNavCallId()).thenReturn(NAV_CALLID);
		when(safRequestContextMock.getUserId()).thenReturn("Z123456");
		when(safRequestContextMock.isUserIdNavAnsatt()).thenReturn(true);
		when(safRequestContextMock.getSecurityContext()).thenReturn(safSecurityContextMock);
		var safRequestCache = Mockito.mock(RequestCache.class);
		when(safRequestContextMock.getRequestCache()).thenReturn(safRequestCache);
		return safRequestContextMock;
	}

	@BeforeEach
	void setUp() {
		super.setUp();
		pep7d = new TilgangsmaskinenBackedPep7dImpl(tilgangsmaskinenConsumer);
	}

	@ParameterizedTest
	@MethodSource("shouldPermitForFagsystemAndTema")
	void shouldPermitForSingleOrNoRelevantTredjepart(Tema tema, String fagsaksystem) {
		when(tilgangsmaskinenConsumer.navIdentHasAccess(any(), any(), any())).thenReturn(PepAnswer.permit());

		TilgangSak tilgangSak = TilgangSak.builder()
				.arkivsaksystem(Arkivsakssystem.GSAK)
				.arkivsaksnummer(ARKIVSAKSNUMMER)
				.tema(tema)
				.fagsaksystem(fagsaksystem)
				.fpAktoerIdList(List.of(IDENTIFIKATOR))
				.k9AktoerIdList(List.of(IDENTIFIKATOR))
				.build();

		boolean hasAccess = pep7d.hasAccess(tilgangSak, createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(1)).navIdentHasAccess(any(), any(), any());

		assertThat(hasAccess).isTrue();
	}

	@ParameterizedTest
	@MethodSource("shouldPermitForFagsystemAndTema")
	void shouldPermitForMultipleRelevantTredjepart(Tema tema, String fagsaksystem) {
		when(tilgangsmaskinenConsumer.navIdentHasAccessBulk(any(), any(), any())).thenReturn(PepAnswer.permit());

		List<String> tredjeparter = List.of(IDENTIFIKATOR, IDENTIFIKATOR_2);

		TilgangSak tilgangSak = TilgangSak.builder()
				.arkivsaksystem(Arkivsakssystem.GSAK)
				.arkivsaksnummer(ARKIVSAKSNUMMER)
				.tema(tema)
				.fagsaksystem(fagsaksystem)
				.fpAktoerIdList(tredjeparter)
				.k9AktoerIdList(tredjeparter)
				.build();

		boolean hasAccess = pep7d.hasAccess(tilgangSak, createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(1)).navIdentHasAccessBulk(any(), any(), any());

		assertThat(hasAccess).isTrue();
	}

	static Stream<Arguments> shouldPermitForFagsystemAndTema() {
		return Stream.of(
				Arguments.of(Tema.FOR, FAGSAKSYSTEM_FORELDREPENGELOSNING),
				Arguments.of(Tema.FRI, FAGSAKSYSTEM_K9),
				Arguments.of(Tema.OMS, FAGSAKSYSTEM_K9)
		);
	}

	@Test
	void shouldPermitWhenRessursIsNull() {
		boolean hasAccess = pep7d.hasAccess(null, createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(0)).navIdentHasAccess(any(), any(), any());

		assertThat(hasAccess).isTrue();
	}


	@Test
	void shouldDeny() {
		when(tilgangsmaskinenConsumer.navIdentHasAccess(any(), any(), any())).thenReturn(PepAnswer.deny(new UkjentEllerTekniskReason()));

		TilgangSak tilgangSak = TilgangSak.builder()
				.arkivsaksystem(Arkivsakssystem.GSAK)
				.arkivsaksnummer(ARKIVSAKSNUMMER)
				.tema(Tema.FOR)
				.fagsaksystem(FAGSAKSYSTEM_FORELDREPENGELOSNING)
				.fpAktoerIdList(List.of(IDENTIFIKATOR))
				.k9AktoerIdList(List.of(IDENTIFIKATOR))
				.build();

		boolean hasAccess = pep7d.hasAccess(tilgangSak, createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(1)).navIdentHasAccess(any(), any(), any());

		assertThat(hasAccess).isFalse();
	}
}