package no.nav.saf.tilgangskontroll.abstraction;

import lombok.NonNull;
import no.nav.saf.tilgangskontroll.SafRequestContext;
import no.nav.saf.tilgangskontroll.pep.Pep;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * BrukerStandalonePepEvaluator brukerEvaluator = new BrukerStandalonePepEvaluator(null, brukerDataFetcher, pep1);
 * SakStandalonePepEvaluator sakEvaluator = new SakStandalonePepEvaluator(brukerEvaluator, sakDataFetcher, pep2);
 * JournalpostStandalonePepEvaluator jpEvaluator= new JournalpostStandalonePepEvaluator(sakEvaluator, jpDataFetcher, pep3);
 *
 * @param <T>
 */

public class UpwardsSecModelTraverser<T extends SecModel> {
    UpwardsSecModelTraverser parent;
    SecModelDataFetcher<T> dataFetcher;
    Pep<T> pep;
    SecModelParameterAdapter<T> parameterAdapter;


    public UpwardsSecModelTraverser(UpwardsSecModelTraverser parent, @NonNull SecModelDataFetcher<T> dataFetcher, @NonNull Pep<T> pep, SecModelParameterAdapter<T> parameterAdapter) {
        this.parent = parent;
        this.dataFetcher = dataFetcher;
        this.pep = pep;
        this.parameterAdapter = parameterAdapter;
        if (parent != null) {
            assert parameterAdapter != null;
        }
    }


    /**
     * This method will fetch and filter data from the repo using a DataFetcher and the parameterContext,
     * and will further filter the result using the configured Pep.
     * If parent is not null, the method will call fetchAndFilterAndEnforce on parent objects all the way to
     * the root of the tree. The root parent will have a null parent.
     * If any of the parent searches results in an empty stream, this either means there are no search results or
     * that all results have been filtered away by access denied in ABAC.
     * @param parameterContext
     * @param accessDecicionContext
     * @return
     */

    public List<T> fetchAndFilterAndEnforce(ParameterContext parameterContext, SafRequestContext accessDecicionContext, SecModelWorld secModelWorld) {
        List<T> secModelResult = dataFetcher.fetchAndFilter(parameterContext);

        Stream<T> allowedResult = secModelResult.stream().filter(e -> pep.hasAccess(e, accessDecicionContext));

        if (parent != null) {
            List<T> collect =
                    allowedResult.filter(e ->
                    {
                        ParameterContext innerParameterContext = parameterAdapter.extractSearchParameter(e);
                        List parentResult = parent.fetchAndFilterAndEnforce(innerParameterContext, accessDecicionContext, secModelWorld);
                        if (parentResult.isEmpty()) {
                            return false;
                        } else {
                            return true;
                        }
                    }
            ).collect(Collectors.toList());
            if (secModelWorld != null) {
                secModelWorld.put(collect);
            }
            return collect;
        } else {
            List<T> collect = allowedResult.collect(Collectors.toList());
            if (secModelWorld != null) {
                secModelWorld.put(collect);
            }
            return collect;
        }


    }

}
