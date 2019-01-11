package no.nav.saf.tjeneste.visningsmodell;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */

@Value
@ToString(exclude = "parent")
@Builder
public class DokumentInfo {
	private final Journalpost parent;

	private final String dokumentInfoId;
	private final String tittel;
	private final String brevkode;
	@Builder.Default
	private final List<LogiskVedlegg> logiskeVedlegg = new ArrayList<>();
	@Builder.Default
	private final List<Dokumentvariant> dokumentvarianter = new ArrayList<>();
}
