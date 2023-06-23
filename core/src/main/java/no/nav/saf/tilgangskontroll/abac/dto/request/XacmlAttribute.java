package no.nav.saf.tilgangskontroll.abac.dto.request;

import lombok.Value;

@Value
public class XacmlAttribute {
    String attributeId;
    String value;
}