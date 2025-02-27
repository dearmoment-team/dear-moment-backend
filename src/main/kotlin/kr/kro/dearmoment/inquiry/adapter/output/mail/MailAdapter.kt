package kr.kro.dearmoment.inquiry.adapter.output.mail

import jakarta.mail.Message
import jakarta.mail.internet.InternetAddress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.kro.dearmoment.inquiry.application.port.output.SendInquiryPort
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class MailAdapter(
    private val mailProperties: MailProperties,
    private val mailSender: JavaMailSender,
) : SendInquiryPort {
    override fun sendMail(
        email: String,
        subject: String,
        body: String,
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val message = mailSender.createMimeMessage()

                message.subject = "[$email $INQUIRY_TITLE_POSTFIX] 제목: $subject"
                message.setRecipient(Message.RecipientType.TO, InternetAddress(mailProperties.receiver))
                message.setFrom(InternetAddress(mailProperties.username, MAIL_SENDER_NAME))
                message.setText(body)

                mailSender.send(message)
            }.onFailure {
                throw it
            }
        }
    }

    companion object {
        private const val INQUIRY_TITLE_POSTFIX = "님이 보내신 문의입니다."
        private const val MAIL_SENDER_NAME = "디어모먼트"
    }
}
