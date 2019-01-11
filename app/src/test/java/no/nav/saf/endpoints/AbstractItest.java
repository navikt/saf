package no.nav.saf.endpoints;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import com.github.tomakehurst.wiremock.client.WireMock;
import no.nav.saf.ApplicationConfig;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.endpoints.testconfig.STSTestConfig;
import org.apache.cxf.helpers.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.inject.Inject;
import java.io.IOException;


/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {ApplicationConfig.class, STSTestConfig.class},
		webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("itest,wiremock")
@ImportAutoConfiguration
@AutoConfigureWireMock(port = 0)
public abstract class AbstractItest {

	private static final String OIDC_TOKEN_PERSON_USER_TEST = "Bearer " + "eyAidHlwIjogIkpXVCIsICJraWQiOiAiaG9ReWxXNDZpYWoyY1VjN3d4TFF2b3pLVTMwPSIsICJhbGciOiAiUlMyNTYiIH0.eyAiYXRfaGFzaCI6ICJ2RDdmT2lYZ2lGam8zcTNBN2FiektBIiwgInN1YiI6ICJaOTkwNzM5IiwgImF1ZGl0VHJhY2tpbmdJZCI6ICI5YmFhZTIyNS1hMTk4LTQ5ZjktYjU3MS01NTA3MGNiNmE3NjMtMzA4ODQwNyIsICJpc3MiOiAiaHR0cHM6Ly9pc3NvLXQuYWRlby5ubzo0NDMvaXNzby9vYXV0aDIiLCAidG9rZW5OYW1lIjogImlkX3Rva2VuIiwgImF1ZCI6ICJpZGEtdCIsICJjX2hhc2giOiAiSE9pd0t1bE1PclRDQXV0c0VrcE1HUSIsICJvcmcuZm9yZ2Vyb2NrLm9wZW5pZGNvbm5lY3Qub3BzIjogImZkZTM5NWEwLTk2NzctNDJjNS04YjllLTRmODkyZjM2YzAzMyIsICJhenAiOiAiaWRhLXQiLCAiYXV0aF90aW1lIjogMTU0NzE5MTE1NiwgInJlYWxtIjogIi8iLCAiZXhwIjogMTU0NzE5NDc1NiwgInRva2VuVHlwZSI6ICJKV1RUb2tlbiIsICJpYXQiOiAxNTQ3MTkxMTU2IH0.ixa5i1IM-k6CPcKFVLsxCwkqRHSff0bh1c64RaSLOZPYIKNh4tRxb6f9oFa6Qv8viKecoBVn2Nczr37AVBPQkgKaFH7iuIpGSVzkU92EiCBxBg318wb0KA6C6v9wFP1OGw3XkfNlT2CSvX13O2XxvU6NoZ6n8OSz_tRQy-MrVTNIOJmolqQ9xqH_4j-vaO_VndZi9ccfbsReZDO6RpfFp5facd1G6lXdcSDI1bnpXM79C6Xn3tYyYnqIL5TfT4K7EQJ6fOktkha4IiGgIDYiMwsgkk7zJhTJHIjMFz9vbBAsC7yKMUXHFqu0dE61hFM-LfUkgtbA1geTXgOYkBgRTw";

	@Inject
	protected TestRestTemplate restTemplate;

	@Inject
	CacheManager cacheManager;

	@BeforeEach
	public void setUp() {
		cacheManager.getCache(LokalCacheConfig.SAK_BY_SAKID_CACHE).clear();
		cacheManager.getCache(LokalCacheConfig.TILGANGSMODELL_REPO_BRUKER_CACHE).clear();
		cacheManager.getCache(LokalCacheConfig.PENSJON_SAK_SAMMENDRAG_LISTE_CACHE).clear();

		WireMock.reset();
		WireMock.resetAllRequests();
		WireMock.removeAllMappings();
	}

	protected HttpEntity createHttpEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
		return new HttpEntity(headers);
	}

	protected HttpHeaders createHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		headers.add(HttpHeaders.AUTHORIZATION, OIDC_TOKEN_PERSON_USER_TEST);
		return headers;
	}

	protected void abacDeny() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-deny.json")));
	}

	protected void abacPermit() {
		stubFor(post(urlEqualTo("/abac"))
				.willReturn(aResponse().withStatus(HttpStatus.OK.value())
						.withHeader(org.apache.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
						.withBodyFile("abac/abac-permit.json")));
	}

	protected String stringFromClasspath(String resourcename) throws IOException {
		return IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream(resourcename));
	}

}
