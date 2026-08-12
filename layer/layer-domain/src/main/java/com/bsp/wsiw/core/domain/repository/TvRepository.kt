package com.bsp.wsiw.core.domain.repository

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.domain.model.DiscoverFilter
import com.bsp.wsiw.core.domain.model.Genre
import com.bsp.wsiw.core.domain.model.PagedResult
import com.bsp.wsiw.core.domain.model.TvShow
import com.bsp.wsiw.core.domain.model.TvShowDetail
import kotlinx.coroutines.flow.Flow

interface TvRepository {
    fun getTvByCategory(category: String, page: Int = 1): Flow<Result<PagedResult<TvShow>>>
    fun discoverTv(genreId: Int?, filter: DiscoverFilter, page: Int = 1): Flow<Result<PagedResult<TvShow>>>
    fun getTvGenres(): Flow<Result<List<Genre>>>
    fun getTvDetail(seriesId: Int): Flow<Result<TvShowDetail>>
}
