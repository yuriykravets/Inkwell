package com.partitionsoft.bookshelf.domain.repository

import com.partitionsoft.bookshelf.domain.model.AiRecommendation
import com.partitionsoft.bookshelf.domain.result.Result

interface RecommendationRepository {
    suspend fun recommend(prompt: String, limit: Int = 6): Result<List<AiRecommendation>>
}

