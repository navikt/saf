saf endringslogg
--------------------

Alle nevneverdige endringer blir dokumentert her. 

* Endringer som påvirker saf GraphQL API skal være prefiksert med `GraphQL: `.
* Endringer som mot formodning ikke er bakoverkompatible markeres i en egen seksjon med (ikke bakoverkompatibelt)

Denne malen er basert på [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [3.3.0] - 2019-02-15
### Endret
* `GraphQL: ` Type `Journalpost` og `DokumentInfo` har fått flere nye felt, nevnt under. Se dokumentasjon i [GraphiQL](https://saf-q1.nais.preprod.local/graphiql) eller [Confluence](https://confluence.adeo.no/display/BOA/saf+-+GraphQL+API+v1) for mer informasjon.
* `GraphQL: ` Type `Journalpost`. Nytt felt `behandlingstema`.
* `GraphQL: ` Type `Journalpost`. Nytt felt `behandlingstemanavn`.
* `GraphQL: ` Type `Journalpost`. Nytt felt `avsenderMottakerLand`.
* `GraphQL: ` Type `Journalpost`. Nytt felt `journalforendeEnhet`.
* `GraphQL: ` Type `Journalpost`. Nytt felt `opprettetAvNavn`.
* `GraphQL: ` Type `Journalpost`. Nytt felt `tilleggsopplysninger`.
* `GraphQL: ` Ny Type `Tilleggsopplysning`.
* `GraphQL: ` Type `DokumentInfo`. Nytt felt `originalJournalpostId`.

## [3.2.3] - 2019-02-15
### Endret
* La til støtte for variantformat FULLVERSJON, og oppdaterte tilhørende dokumentasjon.

## [3.2.2] - 2019-02-14
### Endret
* Tilgangskontroll for fysisk dokument relatert til tema (Pep2d) er endret til å ikke gjøres på midlertidige journalposter.

## [3.2.1] - 2019-02-14
### Endret
* Forbedret logging slik at correlationId og saksbehandler inkluderes i loginnslag, og lagt på itester for midlertidige journalposter.

## [3.2.0] - 2019-02-13
### Endret
* `GraphQL: ` Type `DokumentInfo` har støtte for et nytt felt `dokumentstatus` med en ny enum `Dokumentstatus`.

## [3.1.0] - 2019-02-12
### Endret
* `GraphQL: ` Enum `Variantformat` har støtte for en ny enum `PRODUKSJON_DLF`.

## [3.0.3] - 2019-02-12
### Rettet
* Fikset en feil der alle saker på bruker i gsak ble cachet lokalt av appen. Noe som gjorde at saksbehandlere risikerte å vente 10min på journalposter på ny sak.

## [3.0.2] - 2019-02-11
### Rettet
* Fikset en bug som gjorde at TilgangSak ble gitt feil attributter for midlertidige journalposter.

## [3.0.1] - 2019-02-08
### Rettet
* Fikset en bug som gjorde at metoden toSafArkivsaksystem forårsaket nullpointerexceptions for dokumentoversiktBruker.

## [3.0.0] - 2019-02-08
### Endret
* Utvidet dokumentoversikt til å håndtere skjermet journalpost, dokumentinfo og dokumentvariant
* Implementert itester for alle pep'er  

## [2.0.1] - 2019-02-01
### Rettet
* Fikset en bug som førte til at journalpostmetadata ble filtrert bort på pep2d for midlertidige journalposter.

## [2.0.0] - 2019-01-30
### Endret (ikke bakoverkompatibelt)
* `GraphQL: ` Input Object `FagsakIdInput` endret navn til `FagsakInput`.
* `GraphQL: ` Input Object `FagsakInput` felt `fagsaksnummer` endret til `fagsakId`.
* `GraphQL: ` Query `dokumentoversiktFagsak` endret input argument fra `fagsakId` til `fagsak`.

### Endret 
* Mer robust feilhåndtering av feil fra ABAC PDP.

## [1.0.5] - 2019-01-29
### Endret
* Pep2d er endret til å ikke sjekke geografisk tilgang. Dette er nå implisitt håndtert av Pep1g

## [1.0.4] - 2019-01-24
### Lagt til
* Pep3 er nå en del av tilgangskontrollen. Implementert kall mot bisys for å hente relevante parter og flagg for paragraf19

## [1.0.3] - 2019-01-23
### Rettet
* `GraphQL: ` Typen `Sak` returnerte kun null på `fagsakId`. Rettet mapping. 

## [1.0.2] - 2019-01-21
### Rettet
* Fikset en feil på Pep4 tilgangskontroll på journalstatus. Feil ressurser ble sendt til ABAC. 

## [1.0.0] - 2019-01-16
### Lagt til
Støtter henting av dokumentoversikt på fagsak og bruker. I tillegg til å hente fysiske dokumenter (PDFA osv). Det utføres full tilgangskontroll som sørger for at saksbehandlere kun får se de dataene de har tilgang til å se.  
* saf GraphQL API v1. [Dokumentasjon](https://confluence.adeo.no/display/BOA/saf+-+GraphQL+API+v1).
* saf REST hentdokument. [Dokumentasjon](https://confluence.adeo.no/display/BOA/saf+-+REST+hentdokument?src=contextnavpagetreemode)
* `GraphQL: ` Query `dokumentoversiktBruker` for å kunne spørre etter dokumentoversikten til en bruker.
* `GraphQL: ` Query `dokumentoversiktFagsak` for å kunne spørre etter dokumentoversikten til en fagsak.