package com.coddit.app.domain.usecase.user

import com.coddit.app.domain.model.User
import com.coddit.app.domain.model.LinkedAccount
import com.coddit.app.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(uid: String): Flow<User?> {
        return userRepository.getUserProfile(uid)
    }
}

class GetFollowersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String): Result<List<User>> {
        return userRepository.getFollowers(uid)
    }
}

class LinkAccountUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String, account: LinkedAccount): Result<Unit> {
        return userRepository.linkAccount(uid, account)
    }
}

class UpdateBytesUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(uid: String, delta: Int): Result<Unit> {
        return userRepository.updateBytes(uid, delta)
    }
}

class FollowUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(followerUid: String, followedUid: String): Result<Unit> {
        return userRepository.followUser(followerUid, followedUid)
    }
}

class UnfollowUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(followerUid: String, followedUid: String): Result<Unit> {
        return userRepository.unfollowUser(followerUid, followedUid)
    }
}

class IsFollowingUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(followerUid: String, followedUid: String): Result<Boolean> {
        return userRepository.isFollowing(followerUid, followedUid)
    }
}
