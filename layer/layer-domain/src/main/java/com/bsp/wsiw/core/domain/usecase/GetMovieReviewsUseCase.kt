package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.common.AppDispatchers
import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.FlowUseCase
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.model.Review
import com.bsp.wsiw.core.domain.repository.MovieRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMovieReviewsUseCase @Inject constructor(
    private val repository: MovieRepository,
    dispatchers: AppDispatchers,
) : FlowUseCase<GetMovieReviewsUseCase.Params, PagedResult<Review>>(dispatchers) {

    data class Params(val movieId: Int, val page: Int = 1)

    override fun execute(params: Params): Flow<Result<PagedResult<Review>>> =
        repository.getMovieReviews(movieId = params.movieId, page = params.page)
}
