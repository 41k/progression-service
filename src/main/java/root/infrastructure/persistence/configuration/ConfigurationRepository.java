package root.infrastructure.persistence.configuration;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationRepository extends JpaRepository<ConfigurationEntity, Long> {

	@Query(value = "TRUNCATE TABLE configurations", nativeQuery = true)
	void truncate();

	@Query("FROM ConfigurationEntity configuration WHERE configuration.endTimestamp >= :currentTimestamp")
	List<ConfigurationEntity> getActiveAndPendingConfigurations(long currentTimestamp);
}
