package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
class AbstractMultiPepTest {

	private static final RuntimeException ABAC_EXCEPTION = new RuntimeException("Noe gikk galt ved kall mot ABAC");
	private static final RuntimeException TILGANGSMASKINEN_EXCEPTION = new RuntimeException("Noe gikk galt ved kall mot Tilgangsmaskinen");

	private static final PepAnswer DENY = PepAnswer.deny(new UkjentEllerTekniskReason());
	private static final PepAnswer PERMIT = PepAnswer.permit();

	private AbstractMultiPep<Object> multiPep;

	@Mock
	private Pep<Object> abacPep;
	@Mock
	private Pep<Object> tilgangsmaskinenPep;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@ParameterizedTest
	@MethodSource
	void shouldReturnAnswerBasedOnPrioritization(PepAnswer abacResult,
														PepAnswer tilgangsmaskinenResult,
														boolean prioritizeTilgangsmaskinen,
														PepAnswer expectedResult) {
		multiPep = createMultiPep(true, prioritizeTilgangsmaskinen);

		when(abacPep.hasAccessWithAnswer(any(), any())).thenReturn(abacResult);
		when(tilgangsmaskinenPep.hasAccessWithAnswer(any(), any())).thenReturn(tilgangsmaskinenResult);

		var answer = multiPep.hasAccessWithAnswer(null, null);

		assertThat(answer)
				.usingRecursiveComparison()
				.isEqualTo(expectedResult);
	}

	static Stream<Arguments> shouldReturnAnswerBasedOnPrioritization() {
		return Stream.of(
				//Prioriterer tilgangsmaskinen
				arguments(PERMIT, DENY, true, DENY), //Tilgangsmaskinen deny, abac permit -> deny
				arguments(DENY, PERMIT, true, PERMIT), //Tilgangsmaskinen permit, abac deny -> permit
				arguments(PERMIT, PERMIT, true, PERMIT), //Begge permit -> permit
				arguments(DENY, DENY, true, DENY), //Begge deny -> deny

				//Prioriterer abac
				arguments(PERMIT, DENY, false, PERMIT), //ABAC permit, tilgangsmaskinen deny -> permit
				arguments(DENY, PERMIT, false, DENY), //ABAC deny, tilgangsmaskinen permit -> deny
				arguments(PERMIT, PERMIT, false, PERMIT), //Begge permit -> permit
				arguments(DENY, DENY, false, DENY) //Begge deny -> deny
		);
	}

	@ParameterizedTest
	@MethodSource
	void shouldHandleServiceFailuresBasedOnPrioritization(boolean prioritizeTilgangsmaskinen,
														  FailingService failingService,
														  PepAnswer expectedResult) {
		multiPep = createMultiPep(true, prioritizeTilgangsmaskinen);

		switch (failingService) {
			case ABAC -> {
				when(abacPep.hasAccessWithAnswer(any(), any())).thenThrow(ABAC_EXCEPTION);
				when(tilgangsmaskinenPep.hasAccessWithAnswer(any(), any())).thenReturn(PERMIT);
			}
			case TILGANGSMASKINEN -> {
				when(abacPep.hasAccessWithAnswer(any(), any())).thenReturn(PERMIT);
				when(tilgangsmaskinenPep.hasAccessWithAnswer(any(), any())).thenThrow(TILGANGSMASKINEN_EXCEPTION);
			}
			case BOTH -> {
				when(abacPep.hasAccessWithAnswer(any(), any())).thenThrow(ABAC_EXCEPTION);
				when(tilgangsmaskinenPep.hasAccessWithAnswer(any(), any())).thenThrow(TILGANGSMASKINEN_EXCEPTION);
			}
		}

		var answer = multiPep.hasAccessWithAnswer(null, null);

		assertThat(answer)
				.usingRecursiveComparison()
				.isEqualTo(expectedResult);
	}

	static Stream<Arguments> shouldHandleServiceFailuresBasedOnPrioritization() {
		return Stream.of(
				//Prioriterer tilgangsmaskinen
				arguments(true, FailingService.ABAC, PERMIT), //ABAC feiler -> permit
				arguments(true, FailingService.TILGANGSMASKINEN, DENY), //Tilgangsmaskinen feiler -> deny
				arguments(true, FailingService.BOTH, DENY), //Begge feiler -> deny

				//Prioriterer abac
				arguments(false, FailingService.ABAC, DENY), //ABAC feiler -> deny
				arguments(false, FailingService.TILGANGSMASKINEN, PERMIT), //Tilgangsmaskinen feiler -> permit
				arguments(false, FailingService.BOTH, DENY) //Begge feiler -> deny
		);
	}

	@Test
	void shouldOnlyCallAbacWhenFeatureToggleIsFalse() {
		multiPep = createMultiPep(false, true);

		when(abacPep.hasAccessWithAnswer(any(), any())).thenReturn(PERMIT);

		var answer = multiPep.hasAccessWithAnswer(null, null);
		assertThat(answer.isPermit()).isTrue();
	}

	private AbstractMultiPep<Object> createMultiPep(boolean featureToggle, boolean prioritizeTilgangsmaskinen) {
		return new AbstractMultiPep<>(abacPep, tilgangsmaskinenPep, featureToggle, prioritizeTilgangsmaskinen, "TestPep") {};
	}

	private enum FailingService {
		ABAC, TILGANGSMASKINEN, BOTH
	}
}
