package root.infrastructure;

import org.springframework.data.aerospike.repository.AerospikeRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStateDocumentRepository extends AerospikeRepository<UserStateDocument, String> {
}
