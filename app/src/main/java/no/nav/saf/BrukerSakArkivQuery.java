package no.nav.saf;

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
public class BrukerSakArkivQuery {
	@GraphQLQuery(name = "sakerOgJournalposterBy")
	public Bruker sakerOgJournalposterBy(@GraphQLArgument(name = "aktoerId") @GraphQLNonNull String aktoerId) {
		Faker faker = new Faker();
		return Bruker.builder()
				.aktoerId(aktoerId)
				.fnr(faker.number().digits(11))
				.build();
	}
}
