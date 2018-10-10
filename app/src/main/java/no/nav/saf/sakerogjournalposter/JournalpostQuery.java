package no.nav.saf.sakerogjournalposter;

import com.github.javafaker.Faker;
import io.leangen.graphql.annotations.GraphQLContext;
import io.leangen.graphql.annotations.GraphQLQuery;
import no.nav.saf.domain.kode.AvsenderType;
import no.nav.saf.domain.DokumentInfo;
import no.nav.saf.domain.kode.DokumentStatus;
import no.nav.saf.domain.kode.Dokumentkategori;
import no.nav.saf.domain.kode.JournalTilstand;
import no.nav.saf.domain.Journalpost;
import no.nav.saf.domain.kode.JournalpostStatus;
import no.nav.saf.domain.kode.JournalpostType;
import no.nav.saf.domain.kode.Mottakskanal;
import no.nav.saf.domain.Sak;
import no.nav.saf.domain.kode.Temakode;
import no.nav.saf.domain.kode.TilknyttetJournalpostSom;
import no.nav.saf.domain.kode.Utsendingskanal;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class JournalpostQuery {
    @GraphQLQuery(name = "journalposter")
    public List<Journalpost> journalposter(@GraphQLContext Sak sak) {
        Faker faker = new Faker();
        switch (sak.getTema()) {
            case BID:
                return bidrag(faker);
            case FOR:
                return foreldrepenger(faker);
            default:
                return new ArrayList<>();
        }
    }

    private List<Journalpost> foreldrepenger(Faker faker) {
        return Arrays.asList(
                Journalpost.builder()
                        .journalpostId(faker.number().digits(9))
                        .innhold("Søknad om engangsstønad")
                        .avsender(faker.number().digits(11))
                        .avsenderNavn(faker.name().fullName())
                        .avsenderType(AvsenderType.PERSON)
                        .tema(Temakode.FOR)
                        .type(JournalpostType.INNGAENDE)
                        .status(JournalpostStatus.JOURNALFOERT)
                        .datoJournalfoert(LocalDateTime.now())
                        .kanalReferanseId(UUID.randomUUID().toString())
                        .journalfoerendeEnhet("4810")
                        .mottakskanal(Mottakskanal.NAV_NO)
                        .journalTilstand(JournalTilstand.ENDELIG)
                        .dokumentInfo(Arrays.asList(
                                DokumentInfo.builder()
                                        .dokumentId(faker.number().digits(9))
                                        .tittel("Søknad om engangsstønad")
                                        .dokumenttypeId("I0000499")
                                        .dokumentStatus(DokumentStatus.INNSENDT)
                                        .tilknyttetJournalpostSom(TilknyttetJournalpostSom.HOVEDDOKUMENT)
                                        .dokumentkategori(Dokumentkategori.SOKNAD)
                                        .build(),
                                DokumentInfo.builder()
                                        .dokumentId(faker.number().digits(9))
                                        .tittel("Dokumentasjon på inntekt")
                                        .dokumenttypeId("I0000498")
                                        .dokumentStatus(DokumentStatus.INNSENDT)
                                        .tilknyttetJournalpostSom(TilknyttetJournalpostSom.VEDLEGG)
                                        .dokumentkategori(Dokumentkategori.SOKNAD)
                                        .build()

                        ))
                        .build(),
                Journalpost.builder()
                        .journalpostId(faker.number().digits(9))
                        .innhold("Søknad om engangsstønad")
                        .avsender(faker.number().digits(11))
                        .avsenderNavn(faker.name().fullName())
                        .avsenderType(AvsenderType.PERSON)
                        .tema(Temakode.FOR)
                        .type(JournalpostType.INNGAENDE)
                        .status(JournalpostStatus.JOURNALFOERT)
                        .datoJournalfoert(LocalDateTime.now())
                        .kanalReferanseId(UUID.randomUUID().toString())
                        .journalfoerendeEnhet("4810")
                        .mottakskanal(Mottakskanal.NAV_NO)
                        .journalTilstand(JournalTilstand.ENDELIG)
                        .feilregistrert(true)
                        .build()
                ,
                Journalpost.builder()
                        .journalpostId(faker.number().digits(9))
                        .innhold("Innsendt dokumentasjon: Terminbekreftelse")
                        .avsender(faker.number().digits(11))
                        .avsenderNavn(faker.name().fullName())
                        .avsenderType(AvsenderType.SAMHANDLER)
                        .tema(Temakode.FOR)
                        .type(JournalpostType.INNGAENDE)
                        .status(JournalpostStatus.JOURNALFOERT)
                        .datoJournalfoert(LocalDateTime.now())
                        .kanalReferanseId(UUID.randomUUID().toString())
                        .journalfoerendeEnhet("4810")
                        .mottakskanal(Mottakskanal.NAV_NO)
                        .journalTilstand(JournalTilstand.ENDELIG)
                        .dokumentInfo(Arrays.asList(DokumentInfo.builder()
                                .dokumentId(faker.number().digits(9))
                                .tittel("Terminbekreftelse")
                                .dokumenttypeId("I0000500")
                                .dokumentStatus(DokumentStatus.INNSENDT)
                                .tilknyttetJournalpostSom(TilknyttetJournalpostSom.HOVEDDOKUMENT)
                                .dokumentkategori(Dokumentkategori.SOKNAD)
                                .slettet(true)
                                .build()

                        ))
                        .build()
        );
    }

    private List<Journalpost> bidrag(Faker faker) {
        return Arrays.asList(
                Journalpost.builder()
                        .journalpostId(faker.number().digits(9))
                        .innhold("Søknad om forsørgerbidrag")
                        .avsender(faker.number().digits(11))
                        .avsenderNavn(faker.name().fullName())
                        .avsenderType(AvsenderType.PERSON)
                        .tema(Temakode.BID)
                        .type(JournalpostType.INNGAENDE)
                        .status(JournalpostStatus.JOURNALFOERT)
                        .datoJournalfoert(LocalDateTime.now())
                        .kanalReferanseId(UUID.randomUUID().toString())
                        .journalfoerendeEnhet("4810")
                        .mottakskanal(Mottakskanal.NAV_NO)
                        .journalTilstand(JournalTilstand.ENDELIG)
                        .dokumentInfo(Arrays.asList(
                                DokumentInfo.builder()
                                        .dokumentId(faker.number().digits(9))
                                        .tittel("Søknad om forsørgerbidrag")
                                        .dokumenttypeId("I0000342")
                                        .dokumentStatus(DokumentStatus.INNSENDT)
                                        .tilknyttetJournalpostSom(TilknyttetJournalpostSom.HOVEDDOKUMENT)
                                        .dokumentkategori(Dokumentkategori.SOKNAD)
                                        .build(),
                                DokumentInfo.builder()
                                        .dokumentId(faker.number().digits(9))
                                        .tittel("Dokumentasjon på forsørgerbidrag")
                                        .dokumenttypeId("I0000343")
                                        .dokumentStatus(DokumentStatus.INNSENDT)
                                        .tilknyttetJournalpostSom(TilknyttetJournalpostSom.VEDLEGG)
                                        .dokumentkategori(Dokumentkategori.SOKNAD)
                                        .build()

                        ))
                        .build()
                , Journalpost.builder()
                        .journalpostId(faker.number().digits(9))
                        .innhold("Vedtak om forsørgerbidrag")
                        .avsender(faker.number().digits(11))
                        .avsenderNavn(faker.name().fullName())
                        .avsenderType(AvsenderType.PERSON)
                        .tema(Temakode.BID)
                        .type(JournalpostType.UTGAAENDE)
                        .status(JournalpostStatus.JOURNALFOERT)
                        .datoJournalfoert(LocalDateTime.now())
                        .kanalReferanseId(UUID.randomUUID().toString())
                        .journalfoerendeEnhet("4810")
                        .utsendingskanal(Utsendingskanal.SDP)
                        .journalTilstand(JournalTilstand.ENDELIG)
                        .dokumentInfo(Arrays.asList(
                                DokumentInfo.builder()
                                        .dokumentId(faker.number().digits(9))
                                        .tittel("Vedtak om forsørgerbidrag")
                                        .dokumenttypeId("0000200")
                                        .dokumentStatus(DokumentStatus.FERDIGSTILT)
                                        .tilknyttetJournalpostSom(TilknyttetJournalpostSom.HOVEDDOKUMENT)
                                        .dokumentkategori(Dokumentkategori.BREV)
                                        .build()

                        ))
                        .build()
        );
    }
}
