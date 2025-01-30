package kr.kro.dearmoment.image.application.port.output

interface DeleteImageFromObjectStoragePort {
    fun delete(fileName: String)
}
