package kr.kro.dearmoment.image.adapter.output.objectstorage

import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadRequest
import kr.kro.dearmoment.image.application.port.output.DeleteImageFromObjectStoragePort
import kr.kro.dearmoment.image.application.port.output.UploadImagePort
import kr.kro.dearmoment.image.domain.Image
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@Component
class OracleObjectStorageAdapter(
    private val objectStorageProperties: OracleObjectStorageProperties,
) : UploadImagePort, DeleteImageFromObjectStoragePort {
    override fun upload(
        file: MultipartFile,
        userId: Long,
    ): Image {
        val inputStream = file.inputStream
        val fileDir = "${objectStorageProperties.photoImageDir}$userId"
        val fileName = "$fileDir/${UUID.randomUUID()}-${file.originalFilename}"
        val contentType = "img/${file.contentType?.takeLast(3) ?: "JPG"}"

        val putRequest =
            PutObjectRequest.builder()
                .bucketName(objectStorageProperties.bucketName)
                .namespaceName(objectStorageProperties.namespaceName)
                .objectName(fileName)
                .contentLength(inputStream.available().toLong())
                .contentType(contentType)
                .putObjectBody(inputStream)
                .build()

        val uploadRequest =
            UploadRequest.builder(inputStream, inputStream.available().toLong())
                .allowOverwrite(true)
                .build(putRequest)

        OracleObjectStorageUtil().uploadManager.upload(uploadRequest)

        return toDomain(userId, fileName)
    }

    override fun delete(fileName: String) {
        val request =
            DeleteObjectRequest.builder()
                .namespaceName(objectStorageProperties.namespaceName)
                .bucketName(objectStorageProperties.bucketName)
                .objectName(fileName)
                .build()

        val client = OracleObjectStorageUtil().client
        client.deleteObject(request)
    }

    companion object {
        fun toDomain(
            userId: Long,
            fileName: String,
        ): Image {
            return Image(
                userId = userId,
                fileName = fileName,
            )
        }
    }
}
