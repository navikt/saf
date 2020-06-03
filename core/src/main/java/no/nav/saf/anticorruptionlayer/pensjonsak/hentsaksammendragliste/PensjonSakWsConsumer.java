package no.nav.saf.anticorruptionlayer.pensjonsak.hentsaksammendragliste;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.pensjonsak.domain.PsakSakerTo;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.metrics.Monitor;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListeSakManglerEierenhet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.PensjonSakV1;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.meldinger.WSHentSakSammendragListeRequest;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.meldinger.WSHentSakSammendragListeResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Component
public class PensjonSakWsConsumer {
    private static final String PENSJON_SAK_SOAP_INSTANCE = "pensjonsaksoap";

    private final PensjonSakV1 pensjonSakV1;
    private static final int MILLI_TO_NANO_CONST = 1000000;

    @Inject
    public PensjonSakWsConsumer(PensjonSakV1 pensjonSakV1) {
        this.pensjonSakV1 = pensjonSakV1;
    }


    @Cacheable(cacheNames = LokalCacheConfig.PENSJON_SAK_SAMMENDRAG_LISTE_CACHE, key = "#personident")
    @CircuitBreaker(name = PENSJON_SAK_SOAP_INSTANCE)
    @Retry(name = PENSJON_SAK_SOAP_INSTANCE)
    @Monitor(value = "dok_consumer", extraTags = {"process", "hentSakSammendragListe"}, histogram = true)
    public List<PsakSakerTo> hentSakSammendragListe(final String personident) {
        WSHentSakSammendragListeRequest request = new WSHentSakSammendragListeRequest();
        request.setPersonident(personident);

        if (log.isDebugEnabled()) {
            log.debug("Henter psaker for foedselsnummer={}", personident);
        }

        try {
            WSHentSakSammendragListeResponse response = pensjonSakV1.hentSakSammendragListe(request);

            if (log.isDebugEnabled()) {
                log.debug("Hentet ferdig psaker for foedselsnummer={}", personident);
            }
            return response.getSakSammendragListe().stream().map(saksammendrag ->
                    PsakSakerTo.builder()
                            .sakNr(saksammendrag.getSakId())
                            .arkivSakSystem(Arkivsakssystem.PSAK)
                            .tema(saksammendrag.getArkivtema().getValue())
                            .datoOpprettet(saksammendrag.getSaksperiode().getFom() == null ? null :
                                    jodaToJavaLocalDateTime(saksammendrag.getSaksperiode().getFom().toDateTimeAtStartOfDay().toLocalDateTime()))
                            .build())
                    .collect(Collectors.toList());
        } catch (HentSakSammendragListeSakManglerEierenhet e) {
            throw new SafFunctionalException("Funksjonell feil mot PensjonSak_v1.hentSakSammendragListe. Personen ble funnet, men en av sakene mangler eierenhet.", e);
        } catch (HentSakSammendragListePersonIkkeFunnet e) {
            throw new SafFunctionalException("Funksjonell feil mot PensjonSak_v1.hentSakSammendragListe. Personen ble ikke funnet.", e);
        } catch (SOAPFaultException e) {
            // Se https://jira.adeo.no/browse/TEST-40974 for grunnen til at dette er her
            // Workaround for å komme rundt at pensjon ikke oppfyller kontraktene sine
            if (e.getMessage().contains("cvc-particle 3.1: in element {http://nav.no/tjeneste/virksomhet/pensjonSak/v1}hentSakSammendragListepersonIkkeFunnet of type {http://nav.no/tjeneste/virksomhet/pensjonSak/v1/feil}PersonIkkeFunnet, found </a:hentSakSammendragListepersonIkkeFunnet> (in namespace http://nav.no/tjeneste/virksomhet/pensjonSak/v1), but next item should be feilkilde")) {
                throw new SafFunctionalException("Funksjonell feil mot PensjonSak_v1.hentSakSammendragListe. Personen ble ikke funnet.", e);
            } else {
                throw new SafTechnicalException("Teknisk feil mot PensjonSak_v1.hentSakSammendragListe", e);
            }
        } catch (Exception e) {
            throw new SafTechnicalException("Teknisk feil mot PensjonSak_v1.hentSakSammendragListe", e);
        }
    }

    private static java.time.LocalDateTime jodaToJavaLocalDateTime(org.joda.time.LocalDateTime localDateTime) {
        return java.time.LocalDateTime.of(
                localDateTime.getYear(),
                localDateTime.getMonthOfYear(),
                localDateTime.getDayOfMonth(),
                localDateTime.getHourOfDay(),
                localDateTime.getMinuteOfHour(),
                localDateTime.getSecondOfMinute(),
                localDateTime.getMillisOfSecond() * MILLI_TO_NANO_CONST);
    }
}
