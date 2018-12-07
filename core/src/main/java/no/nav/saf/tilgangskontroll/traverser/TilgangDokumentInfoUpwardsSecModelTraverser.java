package no.nav.saf.tilgangskontroll.traverser;

import no.nav.saf.domain.tilgangsmodell.TilgangDokumentInfo;
import no.nav.saf.tilgangskontroll.abstraction.UpwardsSecModelTraverser;
import no.nav.saf.tilgangskontroll.adapter.TilgangDokumentInfoToTilgangJournalpostParameterAdapter;
import no.nav.saf.tilgangskontroll.datafetcher.TilgangDokumentInfoDataFetcher;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.stereotype.Component;

import javax.inject.Named;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class TilgangDokumentInfoUpwardsSecModelTraverser extends UpwardsSecModelTraverser<TilgangDokumentInfo> {

	public TilgangDokumentInfoUpwardsSecModelTraverser(TilgangJournalpostUpwardsSecModelTraverser tilgangJournalpostUpwardsSecModelTraverser,
													   TilgangDokumentInfoDataFetcher tilgangDokumentInfoDataFetcher,
													   @Named("pep5") Pep<TilgangDokumentInfo> pep5,
													   TilgangDokumentInfoToTilgangJournalpostParameterAdapter tilgangDokumentInfoToTilgangJournalpostParameterAdapter) {
		super(tilgangJournalpostUpwardsSecModelTraverser, tilgangDokumentInfoDataFetcher, pep5, tilgangDokumentInfoToTilgangJournalpostParameterAdapter);
	}
}
