saf endringslogg
--------------------

Alle nevneverdige endringer blir dokumentert her. 

* Endringer som påvirker saf GraphQL API skal være prefiksert med `GraphQL: `.

Denne malen er basert på [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [1.0.0] - 2019-01-16
### Lagt til
Støtter henting av dokumentoversikt på fagsak og bruker. I tillegg til å hente fysiske dokumenter (PDFA osv). Det utføres full tilgangskontroll som sørger for at saksbehandlere kun får se de dataene de har tilgang til å se.  
* saf GraphQL API v1. [Dokumentasjon](https://confluence.adeo.no/display/BOA/saf+-+GraphQL+API+v1).
* saf REST hentdokument. [Dokumentasjon](https://confluence.adeo.no/display/BOA/saf+-+REST+hentdokument?src=contextnavpagetreemode)
* `GraphQL: ` Query `dokumentoversiktBruker` for å kunne spørre etter dokumentoversikten til en bruker.
* `GraphQL: ` Query `dokumentoversiktFagsak` for å kunne spørre etter dokumentoversikten til en fagsak.