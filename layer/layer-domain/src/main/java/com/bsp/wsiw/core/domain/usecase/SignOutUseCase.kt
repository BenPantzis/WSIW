package com.bsp.wsiw.core.domain.usecase

import com.bsp.wsiw.core.domain.repository.AuthRepository
import javax.inject.Inject

class SignOutUseCase @Inject constructor(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() = repository.signOut()
}
