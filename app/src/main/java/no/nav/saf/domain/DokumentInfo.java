package no.nav.saf.domain;

import lombok.Builder;
import lombok.Data;
import no.nav.saf.domain.kode.DokumentStatus;
import no.nav.saf.domain.kode.Dokumentkategori;
import no.nav.saf.domain.kode.TilknyttetJournalpostSom;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class DokumentInfo {
    private final String dokumentId;
    private final String tittel;
    private final String dokumenttypeId;
    private final DokumentStatus dokumentStatus;
    private final Dokumentkategori dokumentkategori;
    private final boolean skjermet;
    private final boolean slettet;
    private final TilknyttetJournalpostSom tilknyttetJournalpostSom;
}

