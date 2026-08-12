package com.bsp.wsiw.core.domain.model

enum class SortBy(val apiValue: String) {
    Popularity("popularity.desc"),
    Rating("vote_average.desc"),
    ReleaseDate("release_date.desc"),
    VoteCount("vote_count.desc"),
}

data class DiscoverFilter(
    val sortBy: SortBy = SortBy.Popularity,
    val minRating: Float? = null,
    val year: Int? = null,
) {
    val isDefault get() = sortBy == SortBy.Popularity && minRating == null && year == null
    val activeCount get() = listOfNotNull(
        if (sortBy != SortBy.Popularity) sortBy else null,
        minRating,
        year,
    ).size
}
