package no.nav.saf.tilgangskontroll.validation.registry;


import java.util.List;
import java.util.Optional;

/**
 * Registry over all the different identity providers registered.
 */
public interface IdpRegistry {
	Optional<Idp> findByIssuer(String iss);
	List<Idp> getAll();
}