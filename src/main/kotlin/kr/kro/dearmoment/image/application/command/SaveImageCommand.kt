package kr.kro.dearmoment.image.application.command

import org.springframework.web.multipart.MultipartFile
import java.util.UUID

data class SaveImageCommand(
    val file: MultipartFile,
    val userId: UUID,
)
