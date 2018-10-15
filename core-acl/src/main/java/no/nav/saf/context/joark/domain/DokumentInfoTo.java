package no.nav.saf.context.joark.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import no.nav.saf.context.joark.domain.kode.DokumentKategoriCode;
import no.nav.saf.context.joark.domain.kode.DokumentStatusCode;

import java.util.Date;

/**
 * @author Ugur Alpay Cenar, Visma Consulting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DokumentInfoTo {

	private Long dokumentInfoId;
	private String brevkode;
	private String brevgruppe;
	private String konvertertFraSystem;
	private Boolean sensitivt;
	private Boolean slettet;
	private String endretAvNavn;
	private DokumentKategoriCode kategori;
	private DokumentStatusCode dokumentstatus;
	private Date dokumentFerdigDato;
	private String tittel;
	private String konfidensialitet;
	private String integritet;
	private String tilgjengelighet;
	private Boolean innskrenketPartsinnsyn;
	private Boolean innskrenketPartsinnsynFraTredjepart;
	private Boolean organInternt;
	private Long originalJournalpostId;
	private String dokumenttypeId;
//    private Set<SkannetInnhold> skannetInnholdListe = new HashSet<>(); TODO Trenger vi denne?
//    private Set<JournalpostDokumentInfoRelasjonTo> journalpostRelasjoner = new HashSet<>(); TODO Trenger vi denne?
//    private Set<Fildetaljer> fildetaljerListe = new HashSet<>(); TODO Trenger vi denne?

}
