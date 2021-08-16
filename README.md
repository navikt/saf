## saf
================

Saf (sak og arkivfasade) er en løsning for å tilby les-tjenester imot NAV sin database Joark, med korrekt og effektiv tilgangsstyring til metadata og dokumenter. Punktvis oppsummert har saf-applikasjonen har som mål å: 
- Tilby raske søk mot Joark med korrekt tilgangsstyring
- Muliggjøre at fagsystemene (Gosys, Modia, Ditt NAV, Arena, Bisys ...) kan velge å søke på bruker, tema, sak, journalpost eller dokument. 
- Håndtere kommunikasjon mot GSAK/PSAK, slik at fagsystemene ikke lenger behøver å forholde seg til arkivsak.
- Videreføre tilgangsbeslutninger til ABAC. Fagsystemene kan sanere egen tilgangskontroll knyttet til visning av dokumenter, da SAF kun returnerer metadata/dokumenter som konsument har eksplisitt tilgang til.
- Sikre god ytelse ved å ta beslutninger knyttet til tilgangskontroll på et så tidlig tidspunkt som mulig, slik at ikke unødvendig mye data må innhentes.

----------

## Kom i gang

Disse instruksjonene gir deg en kopi av applikasjonen og kommer deg i gang med utvikling. Det antas i disse stegene at java 8 eller nyere, git, og et byggverktøy slik som maven eller gradle satt opp på maskinen.

1. Hent koden
Først må applikasjonskoden skaffes, dette kan gjøres ved "git clone <url>", hvor <url> kan bli funnet på kode-repositoriet du ønsker å hente saf sin applikasjonskode fra.


2. Bygg koden
Neste steg er å bygge java-prosjektet med ditt ønskede byggeverktøy, her er maven valgt som et eksempel. Dette kan gjøres med et 
utvikler-IDE som Intellij eller eclipse, men kan også gjøres kommandoen med følgende kommando kjørt fra saf-mappen som ble hentet i forrige steg.
```bash
mvn package
``` 


3. Test koden
For å kjøre saf-applikasjonens tester kan man deretter kjøre.
```bash
mvn clean verify
``` 


4. Sjekk instillinger
For å kjøre saf lokalt må man tilføre en rekke instillinger. Se i "Lokal utvikling" seksjonen for mer detaljer om disse.


5. Kjør koden
Prosjektet skal nå kunne kjøres lokalt ved kommandoen
```bash
java [options] [builtproject.jar]
```
hvor options inneholder instillingene nevnt i forrige steg.


----------

### Avhengigheter

- Java 8 +
- Maven 3.6.0

----------

## Lokal utvikling

For å kjøre saf lokalt er det nødvendig å hente en truststore, sette profil til nais, tilføre en servicebruker og sette miljøvariabler.

 1. [truststore](fasit.adeo.no)
 2. [servicebruker](https://vault.adeo.no/ui/vault/secrets/serviceuser/show/dev/srvsaf)
 3. [miljøvariabler](https://vault.adeo.no/ui/vault/secrets/secret/show/dokument/saf)
----------

## Deploy

Deploy av saf-koden til miljø gjøres lettest ved hjelp av jenkins. 


----------

## Tilgangsstyring

Ved behov for tilgang til Saf kan andre team selv gjøre nødvendige kodeendringer:
- Lag en ny branch med endringene du ønsker for tilgang. Endringene må legges til i alle ønskede miljø i filene: /nais/<miljø>-config.json.
- Legg til audience i ```NO_NAV_SECURITY_JWT_ISSUER_OPENAM_ACCEPTED_AUDIENCE``` eller ```NO_NAV_SECURITY_JWT_ISSUER_RESTSTS_ACCEPTED_AUDIENCE```, avhengig om du bruker OpenAm eller Rest sts.
- Push endringene og lag en pull request. 
- Pull requesten vil bli gått gjennom og merget av noen i Team Dokumentløsninger.

----------

## Henvendelser
Spørsmål om koden eller prosjektet kan rettes til Team Dokumentløsninger på:
[\#Team Dokumentløsninger](https://nav-it.slack.com/client/T5LNAMWNA/C6W9E5GPJ)
