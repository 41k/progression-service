package root.infrastructure.persistence.state;

import org.springframework.data.aerospike.repository.AerospikeRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStateRepository extends AerospikeRepository<UserStateDocument, String> {
}
