package no.nav.saf.tilgangskontroll.pep;

import lombok.extern.slf4j.Slf4j;
import no.nav.saf.tilgangskontroll.pep.reasons.UkjentEllerTekniskReason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
class MultiPep1gTest {

	@Mock
	AbacBackedPep1gImpl abacImplementation;
	@Mock
	TilgangsmaskinenBackedPep1gImpl tilgangsmaskinenImplementation;

	MultiPep1g multiPep1g;
	private static final PepAnswer DENY = PepAnswer.deny(new UkjentEllerTekniskReason());
	private static final PepAnswer PERMIT = PepAnswer.permit();


	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void shouldPrioritizeTilgangsmaskinenWhenPrioritizieTilgangsmaskinenIsTrue() {
		multiPep1g = new MultiPep1g(abacImplementation, tilgangsmaskinenImplementation, true, true);

		when(abacImplementation.hasAccessWithAnswer(any(), any())).thenReturn(DENY);
		when(tilgangsmaskinenImplementation.hasAccessWithAnswer(any(), any())).thenReturn(PERMIT);

		var answer = multiPep1g.hasAccessWithAnswer(null, null);
		assertThat(answer).isEqualTo(PERMIT);
	}

	@Test
	void shouldPrioritizeAbacWhenPrioritizieTilgangsmaskinenIsFalse() {
		multiPep1g = new MultiPep1g(abacImplementation, tilgangsmaskinenImplementation, true, false);

		when(abacImplementation.hasAccessWithAnswer(any(), any())).thenReturn(PERMIT);
		when(tilgangsmaskinenImplementation.hasAccessWithAnswer(any(), any())).thenReturn(DENY);

		var answer = multiPep1g.hasAccessWithAnswer(null, null);
		assertThat(answer).isEqualTo(PERMIT);
	}

	@Test
	void shouldPermitWhenBothPermit() {
		multiPep1g = new MultiPep1g(abacImplementation, tilgangsmaskinenImplementation, true, true);

		when(abacImplementation.hasAccessWithAnswer(any(), any())).thenReturn(PERMIT);
		when(tilgangsmaskinenImplementation.hasAccessWithAnswer(any(), any())).thenReturn(PERMIT);

		var answer = multiPep1g.hasAccessWithAnswer(null, null);
		assertThat(answer).isEqualTo(PERMIT);
	}

	@Test
	void shouldDenyWhenBothDeny() {
		multiPep1g = new MultiPep1g(abacImplementation, tilgangsmaskinenImplementation, true, true);

		when(abacImplementation.hasAccessWithAnswer(any(), any())).thenReturn(DENY);
		when(tilgangsmaskinenImplementation.hasAccessWithAnswer(any(), any())).thenReturn(DENY);

		var answer = multiPep1g.hasAccessWithAnswer(null, null);
		assertThat(answer).isEqualTo(DENY);
	}

	@Test
	void shouldGracefullyHandleExceptionsFromTilgangsmaskinen() {
		multiPep1g = new MultiPep1g(abacImplementation, tilgangsmaskinenImplementation, true, false);

		when(abacImplementation.hasAccessWithAnswer(any(), any())).thenReturn(DENY);
		when(tilgangsmaskinenImplementation.hasAccessWithAnswer(any(), any())).thenThrow(new RuntimeException("Oh no Tilgangsmaskinen exploded!"));

		var answer = multiPep1g.hasAccessWithAnswer(null, null);
		assertThat(answer).isEqualTo(DENY);
	}
}