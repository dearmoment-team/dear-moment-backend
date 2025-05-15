package kr.kro.dearmoment.image.application.port.input

import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.domain.Image

interface SaveImageUseCase {
    fun save(saveImageCommand: SaveImageCommand): Image

    fun saveAll(commands: List<SaveImageCommand>): List<Image>
}
