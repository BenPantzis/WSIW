package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.domain.repository.AuthRepository
import javax.inject.Inject

class CreateUserSessionUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(requestToken: String) = repository.createUserSession(requestToken)
}
