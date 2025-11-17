package root.infrastructure.persistence.configuration;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationRepository extends JpaRepository<ConfigurationEntity, Long>, JpaSpecificationExecutor<ConfigurationEntity> {

	@Query("FROM ConfigurationEntity configuration WHERE configuration.endTimestamp > :currentTimestamp")
	List<ConfigurationEntity> getActiveAndPendingConfigurations(long currentTimestamp);

	// todo: refactor query since not all cases are covered
	@Query(
			"FROM ConfigurationEntity configuration " +
			"WHERE configuration.startTimestamp BETWEEN :startTimestamp AND :endTimestamp " +
			"OR configuration.endTimestamp BETWEEN :startTimestamp AND :endTimestamp"
	)
	List<ConfigurationEntity> getConfigurationsWithTimeRangeIntersection(long startTimestamp, long endTimestamp);

	@Query(value = "TRUNCATE TABLE configurations", nativeQuery = true)
	void truncate();
}
