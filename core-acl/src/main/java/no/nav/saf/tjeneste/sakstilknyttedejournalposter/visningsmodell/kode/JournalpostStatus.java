package no.nav.saf.tjeneste.sakstilknyttedejournalposter.visningsmodell.kode;

import io.leangen.graphql.annotations.GraphQLEnumValue;
import no.nav.saf.anticorruptionlayer.joark.domain.kode.JournalStatusCode;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public enum JournalpostStatus {

    @GraphQLEnumValue(description = "Endelig journalføring")
    J,

    @GraphQLEnumValue(description = "Midlertidig journalføring")
    M,

    @GraphQLEnumValue(description = "Ferdigstilt og sendt videre til sentralprint")
    FS,

    @GraphQLEnumValue(description = "Ferdigstilt og sendt videre til lokalprint")
    FL,

    @GraphQLEnumValue(description = "Utgår")
    U,

    @GraphQLEnumValue(description = "Avbrutt")
    A,

    @GraphQLEnumValue(description = "Dokument under produksjon")
    D,

    @GraphQLEnumValue(description = "Ekspedert")
    E,

    @GraphQLEnumValue(description = "Mottat")
    MO,

    @GraphQLEnumValue(description = "Ukjent bruker")
    UB,

    @GraphQLEnumValue(description = "Opplaster dokument")
    OD,

    @GraphQLEnumValue(description = "Reservert dokument")
    R;

    public static JournalpostStatus fromJoark(JournalStatusCode journalStatusCode) {
        if(journalStatusCode == null) {
            return null;
        }
        return JournalpostStatus.valueOf(journalStatusCode.name());
    }
}
