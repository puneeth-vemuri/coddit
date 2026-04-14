package com.coddit.app.domain.usecase.reply

import com.coddit.app.domain.model.Reply
import com.coddit.app.domain.repository.ReplyRepository
import javax.inject.Inject

class CreateReplyUseCase @Inject constructor(
    private val replyRepository: ReplyRepository
) {
    suspend operator fun invoke(reply: Reply): Result<Unit> {
        return replyRepository.createReply(reply)
    }
}

class AcceptReplyUseCase @Inject constructor(
    private val replyRepository: ReplyRepository
) {
    suspend operator fun invoke(postId: String, replyId: String): Result<Unit> {
        return replyRepository.acceptReply(postId, replyId)
    }
}

class VoteReplyUseCase @Inject constructor(
    private val replyRepository: ReplyRepository
) {
    suspend operator fun invoke(postId: String, replyId: String, voterUid: String): Result<Unit> {
        return replyRepository.voteReply(postId, replyId, voterUid)
    }
}

class UpdateReplyUseCase @Inject constructor(
    private val replyRepository: ReplyRepository
) {
    suspend operator fun invoke(postId: String, replyId: String, body: String): Result<Unit> {
        return replyRepository.updateReply(postId, replyId, body)
    }
}

class DeleteReplyUseCase @Inject constructor(
    private val replyRepository: ReplyRepository
) {
    suspend operator fun invoke(postId: String, replyId: String): Result<Unit> {
        return replyRepository.deleteReply(postId, replyId)
    }
}
