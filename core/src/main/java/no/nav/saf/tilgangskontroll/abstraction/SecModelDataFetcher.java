package no.nav.saf.tilgangskontroll.abstraction;


import java.util.List;

public interface SecModelDataFetcher<T extends SecModel> {

    List<T> fetchAndFilter(ParameterContext parameterContext);
}
