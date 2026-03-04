package vbotelho.dev.algorithm_sm2.service.dto.response;

import java.util.List;

public record BulkReviewResponse(int processed,
                                 int failed,
                                 List<ReviewResponse> results) {
}
