package no.nav.saf.domain;

import io.leangen.graphql.annotations.GraphQLEnumValue;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
public enum JournalpostStatus {

    @GraphQLEnumValue(description = "Endelig journalføring")
    JOURNALFOERT,

    @GraphQLEnumValue(description = "Midlertidig journalføring")
    MIDLERTIDIG_JOURNALFOERT,

    @GraphQLEnumValue(description = "Ferdigstilt og sendt videre til sentralprint")
    FERDIGSTILT_SENTRALPRINT,

    @GraphQLEnumValue(description = "Ferdigstilt og sendt videre til lokalprint")
    FERDIGSTILT_LOKALPRINT,

    @GraphQLEnumValue(description = "Utgår")
    UTGAAR,

    @GraphQLEnumValue(description = "Avbrutt")
    AVBRUTT,

    @GraphQLEnumValue(description = "Dokument under produksjon")
    UNDER_PRODUKSJON,

    @GraphQLEnumValue(description = "Ekspedert")
    EKSPEDERT,

    @GraphQLEnumValue(description = "Mottat")
    MOTTAT,

    @GraphQLEnumValue(description = "Ukjent bruker")
    UKJENT_BRUKER,

    @GraphQLEnumValue(description = "Opplaster dokument")
    OPPLASTER_DOKUMENT,

    @GraphQLEnumValue(description = "Reservert dokument")
    RESERVERT;

}
