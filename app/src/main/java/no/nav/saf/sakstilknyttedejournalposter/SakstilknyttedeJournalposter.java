package no.nav.saf.sakstilknyttedejournalposter;

import com.github.javafaker.Faker;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLNonNull;
import io.leangen.graphql.annotations.GraphQLQuery;
import no.nav.saf.domain.Bruker;
import org.springframework.stereotype.Component;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class SakstilknyttedeJournalposter {
	@GraphQLQuery(name = "sakstilknyttedeJournalposterBy")
	public Bruker sakstilknyttedeJournalposterBy(@GraphQLArgument(name = "aktoerId") @GraphQLNonNull String aktoerId) {
		return Bruker.builder()
				.aktoerId(aktoerId)
				.build();
	}
}
