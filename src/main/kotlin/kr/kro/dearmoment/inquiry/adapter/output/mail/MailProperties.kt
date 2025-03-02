package kr.kro.dearmoment.inquiry.adapter.output.mail

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
data class MailProperties(
    @Value("\${spring.mail.host}")
    val host: String,
    @Value("\${spring.mail.port}")
    val port: Int,
    @Value("\${spring.mail.username}")
    val username: String,
    @Value("\${spring.mail.password}")
    val password: String,
    @Value("\${spring.mail.receiver}")
    val receiver: String,
    @Value("\${spring.mail.properties.mail.smtp.auth}")
    val auth: Boolean,
    @Value("\${spring.mail.properties.mail.smtp.starttls.enable}")
    val starttlsEnable: Boolean,
    @Value("\${spring.mail.properties.mail.smtp.starttls.required}")
    val starttlsRequired: Boolean,
    @Value("\${spring.mail.properties.mail.smtp.connection-timeout}")
    val connectionTimeout: Int,
    @Value("\${spring.mail.properties.mail.smtp.timeout}")
    val timeout: Int,
    @Value("\${spring.mail.properties.mail.smtp.write-timeout}")
    val writeTimeout: Int,
)
