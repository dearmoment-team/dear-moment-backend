package kr.kro.dearmoment.inquiry.adapter.output.mail.event

import kr.kro.dearmoment.inquiry.application.port.output.SendInquiryPort
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class InquiryEventListener(
    private val sendInquiryPort: SendInquiryPort,
) {
    private val logger = LoggerFactory.getLogger(InquiryEventListener::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleCreateInquiryEvent(event: InquiryCreateEvent) {
        try {
            sendInquiryPort.sendMail(event.email, event.subject, event.body)
        } catch (e: Exception) {
            logger.error("[InquiryEventListener.handleCreateInquiryEvent] fail", e)
        }
    }
}
