package no.nav.saf.context.gsak.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GsakSakerTo {
    private Integer id;
    private String tema;
    private String applikasjon;
    private String aktoerId;
    private String orgnr;
    private String fagsakNr;
}
