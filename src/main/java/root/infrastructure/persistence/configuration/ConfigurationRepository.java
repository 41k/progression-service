package root.infrastructure.persistence.configuration;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationRepository extends JpaRepository<ConfigurationEntity, Long>, JpaSpecificationExecutor<ConfigurationEntity> {

	@Query("FROM ConfigurationEntity c WHERE c.endTimestamp > :currentTimestamp")
	List<ConfigurationEntity> getActiveAndPendingConfigurations(long currentTimestamp);

	@Query("FROM ConfigurationEntity c WHERE c.startTimestamp < :endTimestamp AND c.endTimestamp > :startTimestamp")
	List<ConfigurationEntity> getConfigurationsWithTimeRangeIntersection(long startTimestamp, long endTimestamp);

	@Query(value = "TRUNCATE TABLE configurations", nativeQuery = true)
	void truncate();
}
