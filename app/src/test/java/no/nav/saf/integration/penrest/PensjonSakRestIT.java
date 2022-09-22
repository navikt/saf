package no.nav.saf.integration.penrest;

import no.nav.saf.anticorruptionlayer.pensjonsak.domain.SakSammendrag;
import no.nav.saf.anticorruptionlayer.pensjonsak.hentbrukerforsak.PensjonSakRestConsumer;
import no.nav.saf.endpoints.AbstractItest;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.awaitility.Awaitility.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

class PensjonSakRestIT extends AbstractItest {

	@Resource
	private PensjonSakRestConsumer pensjonSakRest;

	@BeforeEach
	public void setup() {
		setupHappyPathAzureToken();
	}

	@Test
	public void PensjonSakSammendragRestLookUpHappyCase() throws InterruptedException {
		stubFor(get(urlMatching(".*/sammendrag"))
				.willReturn(aResponse()
						.withStatus(HttpStatus.OK.value())
						.withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
						.withBodyFile("psak/psak-hentSakSammendragListe-happy-full.json")));

		given().ignoreExceptions().await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
			List<SakSammendrag> sammendragList = pensjonSakRest.hentSakSammendragListe("12345654321");

			assertAll(
					() -> assertThat(sammendragList, hasSize(5)),
					() -> assertThat(sammendragList, hasItem(where(SakSammendrag::saksstatus, equalToIgnoringCase("AVSLUTTET")))),
					() -> assertThat(sammendragList, hasItem(where((Function<SakSammendrag, LocalDate>) sammendrag -> sammendrag.saksperiode().fom(), equalTo(LocalDate.of(2019, 5, 12)))))
			);
		});
	}


	private static <T, V> BaseMatcher<T> where(Function<T, V> function, Matcher<V> matcher) {
		return new BaseMatcher<T>() {
			@Override
			public void describeTo(Description description) {
				description.appendText("a lambda returning ").appendDescriptionOf(matcher);
			}

			@Override
			public boolean matches(Object thing) {
				try {
					if (thing != null) {
						return matcher.matches(function.apply((T) thing));
					}
				} catch (ClassCastException e) {
				}
				return false;
			}
		};
	}
}