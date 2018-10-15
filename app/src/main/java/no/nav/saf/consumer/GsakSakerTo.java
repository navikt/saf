package no.nav.saf.consumer;

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
