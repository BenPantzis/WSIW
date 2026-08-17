package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.domain.repository.AuthRepository
import javax.inject.Inject

class GetRequestTokenUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): String = repository.getRequestToken()
}
