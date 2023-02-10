# Saf
Saf (sak og arkivfasade) er en løsning for å tilby les-tjenester mot NAV sin database Joark, med korrekt og effektiv tilgangsstyring til metadata og dokumenter. Punktvis oppsummert har saf-applikasjonen som mål å: 
- Tilby raske søk mot Joark med korrekt tilgangsstyring.
- Muliggjøre at fagsystemene (Gosys, Modia, Min side, Arena, Bisys etc.) kan velge å søke på bruker, tema, sak, journalpost eller dokument. 
- Håndtere kommunikasjon mot GSAK/PSAK, slik at fagsystemene ikke behøver å forholde seg til arkivsak.
- Videreføre tilgangsbeslutninger til ABAC. Fagsystemene kan sanere egen tilgangskontroll knyttet til visning av dokumenter, da SAF kun returnerer metadata/dokumenter som konsument har eksplisitt tilgang til.
- Sikre god ytelse ved å ta beslutninger knyttet til tilgangskontroll på et så tidlig tidspunkt som mulig, slik at det ikke innhentes unødvendig mye data.

## Kjøring av saf lokalt
For å kjøre saf lokalt er det nødvendig å hente en truststore, sette profil til nais, tilføre en servicebruker og sette miljøvariabler.

 1. [truststore](fasit.adeo.no)
 2. [servicebruker](https://vault.adeo.no/ui/vault/secrets/serviceuser/show/dev/srvsaf)
 3. [miljøvariabler](https://vault.adeo.no/ui/vault/secrets/secret/show/dokument/saf)

## Tilgangsstyring
Ved behov for tilgang til Saf kan andre team selv gjøre nødvendige kodeendringer.

For Azure tokens: 
- Lag en ny branch hvor dere legger til et innslag for appen deres i `AZURE_IAC_RULES`
  - [dev](https://github.com/navikt/saf/blob/master/nais/) (legg til i respektiv *-config.json fil for alle miljøer appen ønsker tilgang)
  - [produksjon](https://github.com/navikt/saf/blob/master/nais/p-config.json)
- Push endringene og lag en pull request.
- Pull requesten vil bli gått gjennom og merget av noen i Team Dokumentløsninger.

For Rest sts eller OpenAm:
- Lag en ny branch med endringene du ønsker for tilgang. Endringene må legges til i alle ønskede miljø i filene: /nais/<miljø>-config.json.
- Legg til audience i `NO_NAV_SECURITY_JWT_ISSUER_OPENAM_ACCEPTED_AUDIENCE` eller `NO_NAV_SECURITY_JWT_ISSUER_RESTSTS_ACCEPTED_AUDIENCE`, avhengig om du bruker OpenAm eller Rest sts.
- Push endringene og lag en pull request. 
- Pull requesten vil bli gått gjennom og merget av noen i Team Dokumentløsninger.

### Henvendelser
Spørsmål om koden eller prosjektet kan rettes til [Slack-kanalen for \#Team Dokumentløsninger](https://nav-it.slack.com/archives/C6W9E5GPJ)

