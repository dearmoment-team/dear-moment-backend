package kr.kro.dearmoment.inquiry.adapter.output.mail

import jakarta.mail.Message
import jakarta.mail.internet.InternetAddress
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
        val message = mailSender.createMimeMessage()

        message.subject = subject
        message.setRecipient(Message.RecipientType.TO, InternetAddress(mailProperties.receiver))
        message.setFrom(InternetAddress(mailProperties.username, MAIL_SENDER_NAME))
        message.setText("내용:\n$body\n\n작성자 이메일: $email")

        mailSender.send(message)
    }

    companion object {
        private const val MAIL_SENDER_NAME = "디어모먼트"
    }
}
