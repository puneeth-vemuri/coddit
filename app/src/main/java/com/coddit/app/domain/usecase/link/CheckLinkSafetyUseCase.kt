package com.coddit.app.domain.usecase.link

import com.coddit.app.domain.model.SafeLink
import com.coddit.app.domain.repository.LinkSafetyRepository
import javax.inject.Inject

class CheckLinkSafetyUseCase @Inject constructor(
    private val linkSafetyRepository: LinkSafetyRepository
) {
    suspend operator fun invoke(url: String): SafeLink {
        return linkSafetyRepository.checkLinkSafety(url)
    }
}
