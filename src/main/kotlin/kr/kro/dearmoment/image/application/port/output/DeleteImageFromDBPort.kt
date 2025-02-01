package kr.kro.dearmoment.image.application.port.output

interface DeleteImageFromDBPort {
    fun delete(imageId: Long)
}
