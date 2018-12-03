package no.nav.saf.tilgangskontroll.traverser;

import no.nav.saf.domain.tilgangsmodell.TilgangJournalpost;
import no.nav.saf.tilgangskontroll.abstraction.UpwardsSecModelTraverser;
import no.nav.saf.tilgangskontroll.adapter.TilgangJournalpostToTilgangSakParameterAdapter;
import no.nav.saf.tilgangskontroll.datafetcher.TilgangJournalpostDataFetcher;
import no.nav.saf.tilgangskontroll.pep.Pep;
import org.springframework.stereotype.Component;

import javax.inject.Named;

/**
 * @author Sigurd Midttun, Visma Consulting.
 */
@Component
public class TilgangJournalpostUpwardsSecModelTraverser extends UpwardsSecModelTraverser<TilgangJournalpost> {

	public TilgangJournalpostUpwardsSecModelTraverser(TilgangSakUpwardsSecModelTraverser tilgangSakUpwardsSecModelTraverser,
													  TilgangJournalpostDataFetcher tilgangJournalpostDataFetcher,
													  @Named("pep4") Pep<TilgangJournalpost> pep4,
													  TilgangJournalpostToTilgangSakParameterAdapter tilgangJournalpostToTilgangSakParameterAdapter) {
		super(tilgangSakUpwardsSecModelTraverser, tilgangJournalpostDataFetcher, pep4, tilgangJournalpostToTilgangSakParameterAdapter);
	}
}
