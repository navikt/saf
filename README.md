# Saf

Saf (sak og arkivfasade) er en løsning for å tilby les-tjenester mot NAV sin database Joark, med korrekt og effektiv tilgangsstyring til metadata og dokumenter. Punktvis oppsummert har saf-applikasjonen som mål å:
- Tilby raske søk mot Joark med korrekt tilgangsstyring.
- Muliggjøre at fagsystemene (Gosys, Modia, Min side, Arena, Bisys etc.) kan velge å søke på bruker, tema, sak, journalpost eller dokument.
- Håndtere kommunikasjon mot GSAK/PSAK, slik at fagsystemene ikke behøver å forholde seg til arkivsak.
- Videreføre tilgangsbeslutninger til ABAC. Fagsystemene kan sanere egen tilgangskontroll knyttet til visning av dokumenter, da SAF kun returnerer metadata/dokumenter som konsument har eksplisitt tilgang til.
- Sikre god ytelse ved å ta beslutninger knyttet til tilgangskontroll på et så tidlig tidspunkt som mulig, slik at det ikke innhentes unødvendig mye data.

## Komme i gang

Kjør tester og bygg appen

```
mvn clean verify
```

### Tilgangsstyring

Ved behov for tilgang til Saf kan andre team selv gjøre nødvendige kodeendringer.

For Entra tokens:
1. Lag en ny branch hvor dere legger til et innslag for appen deres i `AZURE_IAC_RULES`
   - [dev](https://github.com/navikt/saf/blob/master/nais/) (legg til i respektiv `*-config.json` fil for alle miljøer appen ønsker tilgang)
   - [produksjon](https://github.com/navikt/saf/blob/master/nais/p-config.json)
2. Push endringene
3. Lag en pull request
4. Pull requesten vil bli sett på og merget av noen i Team Dokumentløsninger

---

## Henvendelser

Lag en issue i repository.

### For Nav-ansatte

Spørsmål om appen kan stilles på [#team_dokumentløsninger](https://nav-it.slack.com/archives/C6W9E5GPJ)

## Lisens

[MIT](LICENSE.md)

