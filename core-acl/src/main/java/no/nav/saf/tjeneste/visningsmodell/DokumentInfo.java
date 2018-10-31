package no.nav.saf.tjeneste.visningsmodell;

import lombok.Builder;
import lombok.Value;
import no.nav.saf.tjeneste.visningsmodell.kode.DokumentStatus;
import no.nav.saf.tjeneste.visningsmodell.kode.Dokumentkategori;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
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

