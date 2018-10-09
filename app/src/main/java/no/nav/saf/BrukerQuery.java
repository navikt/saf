package no.nav.saf;

import com.github.javafaker.Faker;
import io.leangen.graphql.annotations.GraphQLArgument;
import io.leangen.graphql.annotations.GraphQLQuery;
import no.nav.saf.domain.Bruker;
import org.springframework.stereotype.Component;

/**
 * @author Joakim Bjørnstad, Jbit AS
 */
@Component
public class BrukerQuery {
	@GraphQLQuery(name = "bruker")
	public Bruker bruker(@GraphQLArgument(name = "foedselsnummer") String foedselsnummer,
						 @GraphQLArgument(name = "aktoerId") String aktoerId) {
		Faker faker = new Faker();
		return Bruker.builder()
				.aktoerId(faker.number().digits(15))
				.fnr(faker.number().digits(11))
				.build();
	}
}
