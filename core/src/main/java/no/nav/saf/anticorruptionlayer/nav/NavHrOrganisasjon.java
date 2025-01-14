package no.nav.saf.anticorruptionlayer.nav;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NavHrOrganisasjon(@JsonProperty("nav_org_nr") String organisasjonsnummer) {
}
