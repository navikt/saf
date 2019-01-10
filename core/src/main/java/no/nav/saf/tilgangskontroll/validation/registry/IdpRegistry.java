package no.nav.saf.tilgangskontroll.validation.registry;


import java.util.List;

/**
 * Registry over all the different identity providers registered.
 */
public interface IdpRegistry {
	List<Idp> getAll();
}