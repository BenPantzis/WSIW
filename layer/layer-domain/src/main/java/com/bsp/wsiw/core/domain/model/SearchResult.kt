package com.bsp.wsiw.core.domain.model

sealed interface SearchResult {
    data class MovieResult(val movie: Movie) : SearchResult
    data class TvResult(val show: TvShow) : SearchResult
    data class PersonResult(val person: PersonSummary) : SearchResult
}
