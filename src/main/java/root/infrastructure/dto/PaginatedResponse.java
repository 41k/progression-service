package root.infrastructure.dto;

import java.util.List;

import lombok.Builder;

@Builder
public record PaginatedResponse<T>(
		Integer currentPage,
		Integer pageSize,
		Integer totalPages,
		Long totalElements,
		List<T> data
) {
}
