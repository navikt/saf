package no.nav.saf.tilgangskontroll.abac.service;


import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;

public interface AbacService {

	XacmlResponse evaluate(XacmlRequest request);
}