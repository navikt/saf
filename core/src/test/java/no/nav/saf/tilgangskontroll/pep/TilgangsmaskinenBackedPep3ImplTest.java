package no.nav.saf.tilgangskontroll.pep;

import no.nav.saf.anticorruptionlayer.nav.tilgangsmaskinen.TilgangsmaskinenConsumer;
import no.nav.saf.domain.kode.Tema;
import no.nav.saf.domain.tilgangsmodell.TilgangIdent;
import no.nav.saf.domain.tilgangsmodell.TilgangRelevantTredjepart;
import no.nav.saf.domain.tilgangsmodell.TilgangSak;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.SafSecurityContext;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.util.List;
import java.util.stream.Stream;

import static no.nav.saf.domain.DomainConstants.FAGSAKSYSTEM_BISYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TilgangsmaskinenBackedPep3ImplTest extends AbstractAbacBackedPepTest {

	private static final String IDENTIFIKATOR = "12345678910";
	private static final String IDENTIFIKATOR_2 = "01987654321";

	@Mock
	TilgangsmaskinenConsumer tilgangsmaskinenConsumer;

	private TilgangsmaskinenBackedPep3Impl pep3;

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
		pep3 = new TilgangsmaskinenBackedPep3Impl(tilgangsmaskinenConsumer);
	}

	@ParameterizedTest
	@MethodSource
	void shouldPermitForSingleOrNoRelevantTredjepart(List<TilgangRelevantTredjepart> relevantTredjepart, int expectedCalls) {
		when(tilgangsmaskinenConsumer.navIdentHasAccess(any(), any(), any())).thenReturn(PepAnswer.permit());

		TilgangSak tilgangSak = TilgangSak.builder()
				.tema(Tema.FAR)
				.fagsaksystem(FAGSAKSYSTEM_BISYS)
				.relevanteTredjeparter(relevantTredjepart)
				.build();

		boolean hasAccess = pep3.hasAccess(tilgangSak, createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(expectedCalls)).navIdentHasAccess(any(), any(), any());

		assertThat(hasAccess).isTrue();
	}

	static Stream<Arguments> shouldPermitForSingleOrNoRelevantTredjepart() {
		return Stream.of(
				Arguments.of(List.of(new TilgangRelevantTredjepart(TilgangIdent.builder().identifikator(IDENTIFIKATOR).build())), 1),
				Arguments.of(List.of(), 0)
		);
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class, names = {"BID", "FAR"})
	void shouldPermitForRelevanteTema(Tema tema) {
		when(tilgangsmaskinenConsumer.navIdentHasAccess(any(), any(), any())).thenReturn(PepAnswer.permit());

		TilgangSak tilgangSak = TilgangSak.builder()
				.tema(tema)
				.fagsaksystem(FAGSAKSYSTEM_BISYS)
				.relevanteTredjeparter(List.of(new TilgangRelevantTredjepart(TilgangIdent.builder().identifikator(IDENTIFIKATOR).build())))
				.build();

		boolean hasAccess = pep3.hasAccess(tilgangSak, createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(1)).navIdentHasAccess(any(), any(), any());

		assertThat(hasAccess).isTrue();
	}

	@ParameterizedTest
	@EnumSource(value = Tema.class, names = {"BID", "FAR"}, mode = EnumSource.Mode.EXCLUDE)
	void shouldPermitForNonRelevanteTema(Tema tema) {
		TilgangSak tilgangSak = TilgangSak.builder()
				.tema(tema)
				.fagsaksystem(FAGSAKSYSTEM_BISYS)
				.relevanteTredjeparter(List.of(new TilgangRelevantTredjepart(TilgangIdent.builder().identifikator(IDENTIFIKATOR).build())))
				.build();

		boolean hasAccess = pep3.hasAccess(tilgangSak, createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(0)).navIdentHasAccess(any(), any(), any());

		assertThat(hasAccess).isTrue();
	}

	@Test
	void shouldPermitForFagsaksystemNotBisys() {
		TilgangSak tilgangSak = TilgangSak.builder()
				.tema(Tema.FAR)
				.fagsaksystem("IKKE BISYS")
				.relevanteTredjeparter(List.of(new TilgangRelevantTredjepart(TilgangIdent.builder().identifikator(IDENTIFIKATOR).build())))
				.build();

		boolean hasAccess = pep3.hasAccess(tilgangSak, createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(0)).navIdentHasAccess(any(), any(), any());

		assertThat(hasAccess).isTrue();
	}

	@Test
	void shouldPermitWhenRessursIsNull() {
		boolean hasAccess = pep3.hasAccess(null, createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(0)).navIdentHasAccess(any(), any(), any());

		assertThat(hasAccess).isTrue();
	}

	@Test
	void shouldPermitForMultipleRelevantTredjepart() {
		when(tilgangsmaskinenConsumer.navIdentHasAccessBulk(any(), any(), any())).thenReturn(PepAnswer.permit());

		List<TilgangRelevantTredjepart> tredjeparter = List.of(
				new TilgangRelevantTredjepart(TilgangIdent.builder()
						.identifikator(IDENTIFIKATOR)
						.build()),
				new TilgangRelevantTredjepart(TilgangIdent.builder()
						.identifikator(IDENTIFIKATOR_2)
						.build())
		);

		TilgangSak tilgangSak = TilgangSak.builder()
				.tema(Tema.FAR)
				.fagsaksystem(FAGSAKSYSTEM_BISYS)
				.relevanteTredjeparter(tredjeparter)
				.build();

		boolean hasAccess = pep3.hasAccess(tilgangSak, createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(1)).navIdentHasAccessBulk(any(), any(), any());

		assertThat(hasAccess).isTrue();
	}

	@Test
	void shouldDeny() {
		when(tilgangsmaskinenConsumer.navIdentHasAccess(any(), any(), any())).thenReturn(PepAnswer.deny(new UkjentEllerTekniskReason()));

		TilgangSak tilgangSak = TilgangSak.builder()
				.tema(Tema.FAR)
				.fagsaksystem(FAGSAKSYSTEM_BISYS)
				.relevanteTredjeparter(List.of(new TilgangRelevantTredjepart(TilgangIdent.builder().identifikator(IDENTIFIKATOR).build())))
				.build();

		boolean hasAccess = pep3.hasAccess(tilgangSak, createSafRequestContext());

		verify(tilgangsmaskinenConsumer, times(1)).navIdentHasAccess(any(), any(), any());

		assertThat(hasAccess).isFalse();
	}
}