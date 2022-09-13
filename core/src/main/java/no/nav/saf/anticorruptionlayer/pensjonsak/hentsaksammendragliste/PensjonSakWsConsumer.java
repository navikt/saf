package no.nav.saf.anticorruptionlayer.pensjonsak.hentsaksammendragliste;


import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import no.nav.saf.anticorruptionlayer.pensjonsak.domain.PsakSakerTo;
import no.nav.saf.cache.LokalCacheConfig;
import no.nav.saf.domain.kode.Arkivsakssystem;
import no.nav.saf.exceptions.SafFunctionalException;
import no.nav.saf.exceptions.SafTechnicalException;
import no.nav.saf.integration.penrest.HentSakSammendragListeResponse;
import no.nav.saf.integration.penrest.PensjonSakRest;
import no.nav.saf.integration.penrest.SakSammendrag;
import no.nav.saf.metrics.Monitor;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListePersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.pensjonsak.v1.HentSakSammendragListeSakManglerEierenhet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import javax.xml.ws.soap.SOAPFaultException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;


@Slf4j
@Component
public class PensjonSakWsConsumer {
    private static final String PENSJON_SAK_SOAP_INSTANCE = "pensjonsaksoap";

    private final PensjonSakRest pensjonSakRest;

    @Autowired
    public PensjonSakWsConsumer(PensjonSakRest pensjonSakRest) {
        this.pensjonSakRest = pensjonSakRest;
    }


    @Cacheable(cacheNames = LokalCacheConfig.PENSJON_SAK_SAMMENDRAG_LISTE_CACHE, key = "#personident", condition = "#p0 != null")
    @CircuitBreaker(name = PENSJON_SAK_SOAP_INSTANCE)
    @Retry(name = PENSJON_SAK_SOAP_INSTANCE)
    @Monitor(value = "dok_consumer", extraTags = {"process", "hentSakSammendragListe"}, histogram = true)
    public List<PsakSakerTo> hentSakSammendragListe(final String personident) {
        if(isBlank(personident)) {
            return new ArrayList<>();
        }

        if (log.isDebugEnabled()) {
            log.debug("Henter psaker for foedselsnummer={}", personident);
        }

        try {
            List<SakSammendrag> response = pensjonSakRest.hentSakSammendragListe(personident);

            if (log.isDebugEnabled()) {
                log.debug("Hentet ferdig psaker for foedselsnummer={}", personident);
            }
            return response.stream().map(saksammendrag ->
                    PsakSakerTo.builder()
                            .sakNr(saksammendrag.sakId())
                            .arkivSakSystem(Arkivsakssystem.PSAK)
                            .tema(saksammendrag.arkivtema().value())
                            .datoOpprettet(saksammendrag.saksperiode().fom() == null ? null :
                                    saksammendrag.saksperiode().fom().atStartOfDay())
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
}
