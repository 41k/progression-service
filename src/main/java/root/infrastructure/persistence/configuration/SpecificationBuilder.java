package root.infrastructure.persistence.configuration;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import root.infrastructure.dto.ConfigurationsFilterDto;

@Component
public class SpecificationBuilder {

	private static final String LIKE_EXPRESSION_FORMAT = "%%%s%%";

	public Specification<ConfigurationEntity> build(ConfigurationsFilterDto filter) {
		var specification = Specification.<ConfigurationEntity>where(null);
		if (isNotBlank(filter.name())) {
			specification = specification.and(likeSpecification("name", filter.name()));
		}
		if (filter.startTimestamp() != null) {
			specification = specification.and(greaterThanOrEqualToSpecification("startTimestamp", filter.startTimestamp()));
		}
		if (filter.endTimestamp() != null) {
			specification = specification.and(lessThanSpecification("endTimestamp", filter.endTimestamp()));
		}
		return specification;
	}

	private <T> Specification<T> likeSpecification(String entityPropertyName, String value) {
		return (root, query, builder) -> builder.like(root.get(entityPropertyName), LIKE_EXPRESSION_FORMAT.formatted(value));
	}

	private <T> Specification<T> equalsSpecification(String entityPropertyName, Object value) {
		return (root, query, builder) -> builder.equal(root.get(entityPropertyName), value);
	}

	private <U extends Comparable<U>, T> Specification<T> greaterThanOrEqualToSpecification(String entityPropertyName, U value) {
		return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get(entityPropertyName), value);
	}

	private <U extends Comparable<U>, T> Specification<T> lessThanSpecification(String entityPropertyName, U value) {
		return (root, query, builder) -> builder.lessThan(root.get(entityPropertyName), value);
	}
}
