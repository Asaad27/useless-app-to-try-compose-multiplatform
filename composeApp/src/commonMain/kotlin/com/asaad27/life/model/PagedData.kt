package com.asaad27.life.model

data class PagedData<T>(
    val items: List<T>,
    val hasMore: Boolean,
    val nextKey: String?
)