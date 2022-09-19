package no.nav.saf.anticorruptionlayer.pensjonsak.hentbrukerforsak;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.config.SafProperties;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.integration.azure.TokenConsumer;
import no.nav.saf.anticorruptionlayer.pensjonsak.domain.SakSammendrag;
import no.nav.saf.metrics.Monitor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.UnknownContentTypeException;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static no.nav.saf.headers.NavHeaders.NAV_CALLID;
import static no.nav.saf.util.MDCUtility.getCallId;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class PensjonSakRestConsumer {
    private static final String PENSJON_SAK_BRUKER_REST_INSTANCE = "pensjonsakrestbruker";
    private static final String PENSJON_SAK_SAMMENDRAG_REST_INSTANCE = "pensjonsakrestsammendrag";

    private final RestTemplate restTemplate;
    private final String pensjonsakApiUrl;
    private final TokenConsumer tokenConsumer;
    private final String pensjonSakScope;
    private final URI pensjonSakSammendragURI;

    public PensjonSakRestConsumer(RestTemplateBuilder restTemplateBuilder,
                                  ClientHttpRequestFactory clientHttpRequestFactory,
                                  @Value("${pensjonsakrs.v1.url}") String pensjonsakApiUrl,
                                  TokenConsumer tokenConsumer,
                                  SafProperties safProperties
    ) {
        this.pensjonsakApiUrl = pensjonsakApiUrl;
        this.tokenConsumer = tokenConsumer;
        this.pensjonSakScope = safProperties.getEndpoints().getPenScope();
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> clientHttpRequestFactory)
                .build();
        this.pensjonSakSammendragURI = URI.create(safProperties.getEndpoints().getPen() + "/sammendrag");
    }

    @Retry(name = PENSJON_SAK_BRUKER_REST_INSTANCE)
    @CircuitBreaker(name = PENSJON_SAK_BRUKER_REST_INSTANCE)
    public HentBrukerForSakResponseTo hentBrukerForSak(final String sakId) {
        try {
            HttpHeaders headers = createHeaders();
            headers.add("sakId", sakId);
            HentBrukerForSakResponseTo hentBrukerForSakResponseTo = restTemplate.exchange(pensjonsakApiUrl, HttpMethod.GET, new HttpEntity<>(headers), HentBrukerForSakResponseTo.class)
                    .getBody();
            if (hentBrukerForSakResponseTo == null || hentBrukerForSakResponseTo.getFnr() == null || hentBrukerForSakResponseTo.getFnr().isEmpty()) {
                throw new SafFunctionalException(String.format("hentBrukerForSak returnerte tomt fødselsnummer for sakId=%s. Dette betyr at saken ikke finnes eller at ingen personer er tilknyttet denne saken", sakId));
            } else {
                return hentBrukerForSakResponseTo;
            }
        } catch (HttpServerErrorException e) {
            throw new SafTechnicalException(String.format("hentBrukerForSak feilet teknisk med statusKode=%s. Feilmelding=%s", e
                    .getStatusCode(), e.getMessage()), e, e.getStatusCode());
        } catch (HttpClientErrorException e) {
            throw new SafFunctionalException(String.format("hentBrukerForSak feilet funksjonelt med statusKode=%s. Feilmelding=%s", e
                    .getStatusCode(), e.getMessage()), e, e.getStatusCode());
        }
    }

    @Retry(name = PENSJON_SAK_SAMMENDRAG_REST_INSTANCE)
    @CircuitBreaker(name = PENSJON_SAK_SAMMENDRAG_REST_INSTANCE)
    @Cacheable(cacheNames = LokalCacheConfig.PENSJON_SAK_SAMMENDRAG_LISTE_CACHE, key = "#personident", condition = "#p0 != null")
    @Monitor(value = "dok_consumer", extraTags = {"process", "hentSakSammendragListe"}, histogram = true)
    public List<SakSammendrag> hentSakSammendragListe(String personident) {
        if(isBlank(personident)) {
            return Collections.emptyList();
        }

        if (log.isDebugEnabled()) {
            log.debug("Henter psaker for foedselsnummer={}", personident);
        }

        try {
            HttpHeaders headers = createHeaders();
            headers.add("fnr", personident);

            List<SakSammendrag> result = Objects.requireNonNullElse(
                    restTemplate.exchange(pensjonSakSammendragURI, HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<List<SakSammendrag>>() {
                    }).getBody(),
                    Collections.emptyList());

            if (log.isDebugEnabled()) {
                log.debug("Hentet ferdig psaker for foedselsnummer={}", personident);
            }
            return result;
        } catch (UnknownContentTypeException e) {
            throw new SafTechnicalException(String.format("hentSakSammendrag feilet teknisk med statusKode=%s. Feilmelding=%s", "uhhhhh", e.getMessage()), e);
        } catch (HttpServerErrorException e) {
            throw new SafTechnicalException(String.format("hentSakSammendrag feilet teknisk med statusKode=%s. Feilmelding=%s", e
                    .getStatusCode(), e.getMessage()), e, e.getStatusCode());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                // TODO: dobbeltsjekk at det stemmer at vi får denne for saksammendrag ikke funnet
                throw new SafFunctionalException(String.format("hentSakSammendrag feilet funksjonelt med statusKode=%s. Feilmelding=%s", e
                        .getStatusCode(), e.getMessage()), e, e.getStatusCode());
            }
            throw new SafFunctionalException(String.format("hentSakSammendrag feilet funksjonelt med statusKode=%s. Feilmelding=%s", e
                    .getStatusCode(), e.getMessage()), e, e.getStatusCode());
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        headers.setBearerAuth(tokenConsumer.getClientCredentialToken(pensjonSakScope).getAccess_token());
        headers.set(NAV_CALLID, getCallId());
        return headers;
    }}
