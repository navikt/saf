package no.nav.saf.tilgangskontroll.abstraction;


/**
 * Evaluerer tilgang til ressurs. Implementasjonen bør være request-scoped eller aller helst tilstandsløs.
 * AccessDecisionContext kan brukes til å ta vare på tidligere tilgangsbeslutninger,
 * slik at pdp ikke kalles unødig.
 * @param <T> Ressursetypen som evalueres - action er alltid READ
 */
public interface Pep<T extends SecModel> {
    boolean hasAccesOn(T resource, AccessDecisionContext accessDecisionContext);
}
