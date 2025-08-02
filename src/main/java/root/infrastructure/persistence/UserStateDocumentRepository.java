package root.infrastructure.persistence;

import org.springframework.data.aerospike.repository.AerospikeRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStateDocumentRepository extends AerospikeRepository<UserStateDocument, String> {
}
