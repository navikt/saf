package no.nav.saf.query.dokumentoversikt.arguments;

import graphql.schema.DataFetchingEnvironment;
import lombok.Value;
import no.nav.saf.domain.kode.Journalposttype;
import no.nav.saf.domain.kode.Journalstatus;
import no.nav.saf.domain.kode.Tema;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Value
public class DokumentoversiktFilters {
	private final LocalDate fraDato;
	private final List<Tema> tema;
	private final List<Journalposttype> journalposttyper;
	private final List<Journalstatus> journalstatuser;
	private final boolean visFeilregistrerte;

	DokumentoversiktFilters(LocalDate fraDato, List<Tema> tema, List<Journalposttype> journalposttyper, List<Journalstatus> journalstatuser) {
		if (fraDato == null) {
			this.fraDato = LocalDate.of(1, 1, 1);
		} else {
			this.fraDato = fraDato;
		}
		if (tema.isEmpty()) {
			this.tema = Tema.asList();
		} else {
			this.tema = new ArrayList<>(tema);
		}
		if (journalposttyper.isEmpty()) {
			this.journalposttyper = Journalposttype.asList();
		} else {
			this.journalposttyper = new ArrayList<>(journalposttyper);
		}
		if (journalstatuser.isEmpty()) {
			this.journalstatuser = Journalstatus.asList();
		} else {
			this.journalstatuser =  new ArrayList<>(journalstatuser);
		}
		this.visFeilregistrerte = this.journalstatuser.contains(Journalstatus.FEILREGISTRERT);
	}

	public static DokumentoversiktFilters create(DataFetchingEnvironment environment) {
		LocalDate fraDato = environment.getArgument("fraDato");
		List<Tema> tema = environment.getArgument("tema");
		List<Journalposttype> journalposttyper = getJournalposttypeList(environment);
		List<Journalstatus> journalstatuser = getJournalstatusList(environment);
		return new DokumentoversiktFilters(fraDato, tema, journalposttyper, journalstatuser);
	}

	private static List<Journalposttype> getJournalposttypeList(DataFetchingEnvironment environment) {
		List<Object> journalstatuserObjectList = environment.getArgument("journalposttyper");
		return journalstatuserObjectList.stream()
				.map(journalstatus -> Journalposttype.valueOf(journalstatus.toString()))
				.collect(Collectors.toList());
	}

	private static List<Journalstatus> getJournalstatusList(DataFetchingEnvironment environment) {
		List<Object> journalstatuserObjectList = environment.getArgument("journalstatuser");
		return journalstatuserObjectList.stream()
				.map(journalstatus -> Journalstatus.valueOf(journalstatus.toString()))
				.collect(Collectors.toList());
	}
}
