saf endringslogg
--------------------

Alle nevneverdige endringer blir dokumentert her. 

* Endringer som påvirker saf GraphQL API skal være prefiksert med `GraphQL: `.
* Endringer som mot formodning ikke er bakoverkompatible markeres i en egen seksjon med (ikke bakoverkompatibelt)

Denne malen er basert på [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

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