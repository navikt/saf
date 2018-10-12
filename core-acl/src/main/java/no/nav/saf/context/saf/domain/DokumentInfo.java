package no.nav.saf.context.saf.domain;

import lombok.Builder;
import lombok.Data;
import no.nav.saf.context.saf.domain.kode.DokumentStatus;
import no.nav.saf.context.saf.domain.kode.Dokumentkategori;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Data
@Builder
public class DokumentInfo {
    private final String dokumentID;
    private final String tittel;
    private final Dokumentkategori dokumentkategori;
    private final String dokumenttypeID;
    private final String navSkjemaID;
    private final String behandlingstema;
    private final DokumentStatus dokumentStatus;
    private final boolean synligForEksternBruker;
    private final boolean synligForSaksbehandler;
}

