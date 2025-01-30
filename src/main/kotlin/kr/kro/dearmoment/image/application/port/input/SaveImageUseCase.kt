package kr.kro.dearmoment.image.application.port.input

import kr.kro.dearmoment.image.application.command.SaveImageCommand

interface SaveImageUseCase {
    fun save(saveImageCommand: SaveImageCommand): Long
}
