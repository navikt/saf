package no.nav.saf.query.dokumentoversikt.fagsak;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import graphql.schema.DataFetchingEnvironment;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
class DokumentoversiktFagsakArgumentsTest {
	@Test
	void shouldConstructDokumentoversiktFagsak() {
		final DataFetchingEnvironment mockDataFetchingEnvironment = Mockito.mock(DataFetchingEnvironment.class);
		when(mockDataFetchingEnvironment.getArgument(eq("foerste"))).thenReturn(1);
		when(mockDataFetchingEnvironment.getArgument(eq("fagsak"))).thenReturn(Map.of("fagsakId", "11111", "fagsaksystem", "AO01"));
		assertNotNull(DokumentoversiktFagsakArguments.create(mockDataFetchingEnvironment));
	}

	@Test
	void shouldNotAllowDokumentoversiktFagsakInfotrygdQuery() {
		final DataFetchingEnvironment mockDataFetchingEnvironment = Mockito.mock(DataFetchingEnvironment.class);
		when(mockDataFetchingEnvironment.getArgument(eq("foerste"))).thenReturn(1);
		when(mockDataFetchingEnvironment.getArgument(eq("fagsak"))).thenReturn(Map.of("fagsakId", "01ABC", "fagsaksystem", "IT01"));
		assertThrows(UnsupportedFagsakSystemException.class, () -> DokumentoversiktFagsakArguments.create(mockDataFetchingEnvironment));
	}
}