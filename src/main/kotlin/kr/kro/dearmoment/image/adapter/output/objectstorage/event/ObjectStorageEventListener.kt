package kr.kro.dearmoment.image.adapter.output.objectstorage.event

import kr.kro.dearmoment.image.adapter.output.objectstorage.OracleObjectStorageAdapter
import kr.kro.dearmoment.inquiry.adapter.output.mail.event.InquiryEventListener
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ObjectStorageEventListener(
    private val objectStorageAdapter: OracleObjectStorageAdapter,
) {
    private val logger = LoggerFactory.getLogger(InquiryEventListener::class.java)

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun processImageDelete(event: ImageDeleteEvent) {
        try {
            objectStorageAdapter.delete(event.parId, event.fileName)
        } catch (e: Exception) {
            logger.error("[ObjectStorageEventListener.processImageDelete] fail", e)
        }
    }
}
