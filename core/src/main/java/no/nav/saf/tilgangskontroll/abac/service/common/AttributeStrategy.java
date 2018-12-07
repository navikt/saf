package no.nav.saf.tilgangskontroll.abac.service.common;

import no.nav.saf.tilgangskontroll.abac.dto.request.XacmlRequest;
import no.nav.saf.tilgangskontroll.abac.dto.response.XacmlResponse;

public interface AttributeStrategy<T> {
    boolean isSupported(String attributeId);

    void perform(T attribute, XacmlRequest request, XacmlResponse response);
}