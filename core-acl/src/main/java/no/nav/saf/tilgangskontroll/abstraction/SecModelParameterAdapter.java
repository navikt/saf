package no.nav.saf.tilgangskontroll.abstraction;

/**
 * Classes which implements this interface are expected to
 * extract the neccesary search keys from the secModelResult to populate the parent SecModel entity.
 * @param <T>
 */
public interface SecModelParameterAdapter<T> {
    /**
     * Extract the neccesary search keys from the secModelResult to populate the parent SecModel entity.
     * For Journalpost, the necessary keys to populate the above level is saksref + arkivsaksystem,
     * For Sak, the necessary key to populate the above level is aktoerId.
     *
     * The resulting map will be appended into the ParameterContext in the StandalonePepEvaluator
     * @param secModelEntity
     * @return map of search parameters
     */
    ParameterContext extractSearchParameter(T secModelEntity);
}
