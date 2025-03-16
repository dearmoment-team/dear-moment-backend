package kr.kro.dearmoment.image.application.command

import org.springframework.web.multipart.MultipartFile

data class SaveImageCommand(
    val file: MultipartFile,
    val userId: Long,
)
