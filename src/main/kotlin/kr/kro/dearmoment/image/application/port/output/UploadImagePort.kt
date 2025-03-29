package kr.kro.dearmoment.image.application.port.output

import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.domain.Image
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

interface UploadImagePort {
    fun upload(
        file: MultipartFile,
        userId: UUID,
    ): Image

    fun uploadAll(commands: List<SaveImageCommand>): List<Image>
}
