package kr.kro.dearmoment.user.adapter.output.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WithdrawalFeedbackRepository :
    JpaRepository<WithdrawalFeedbackEntity, Long>