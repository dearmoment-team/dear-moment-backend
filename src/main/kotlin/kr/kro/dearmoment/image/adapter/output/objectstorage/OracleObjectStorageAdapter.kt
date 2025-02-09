package kr.kro.dearmoment.image.adapter.output.objectstorage

import com.oracle.bmc.Region
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails.AccessType
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest
import com.oracle.bmc.objectstorage.requests.DeletePreauthenticatedRequestRequest
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadRequest
import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.application.port.output.DeleteImageFromObjectStoragePort
import kr.kro.dearmoment.image.application.port.output.GetImageFromObjectStoragePort
import kr.kro.dearmoment.image.application.port.output.UploadImagePort
import kr.kro.dearmoment.image.domain.Image
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.time.ZoneId
import java.util.Date
import java.util.UUID

@Component
class OracleObjectStorageAdapter(
    private val objectStorageProperties: OracleObjectStorageProperties,
    private val objectStorageUtil: OracleObjectStorageUtil,
) : UploadImagePort, DeleteImageFromObjectStoragePort, GetImageFromObjectStoragePort {
    private val baseUrl =
        "https://" + objectStorageProperties.namespaceName +
            ".objectstorage." + Region.AP_CHUNCHEON_1.regionId + ".oci.customer-oci.com"

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

        objectStorageUtil.uploadManager.upload(uploadRequest)

        val image =
            Image(
                userId = userId,
                fileName = fileName,
            )

        return getImage(image)
    }

    override fun uploadAll(commands: List<SaveImageCommand>): List<Image> {
        return commands.map { upload(it.file, it.userId) }
    }

    override fun delete(image: Image) {
        val request =
            DeleteObjectRequest.builder()
                .namespaceName(objectStorageProperties.namespaceName)
                .bucketName(objectStorageProperties.bucketName)
                .objectName(image.fileName)
                .build()

        deletePreAuth(image.parId)

        val client = OracleObjectStorageUtil().client
        client.deleteObject(request)
    }

    override fun getImage(image: Image): Image {
        deletePreAuth(image.parId)

        val expireTime = Date(System.currentTimeMillis() + TWO_HOURS_FOR_SECONDS)

        val details =
            CreatePreauthenticatedRequestDetails.builder()
                .accessType(AccessType.ObjectReadWrite)
                .objectName(image.fileName)
                .timeExpires(expireTime)
                .name(image.fileName)
                .build()

        val request =
            CreatePreauthenticatedRequestRequest.builder()
                .namespaceName(objectStorageProperties.namespaceName)
                .bucketName(objectStorageProperties.bucketName)
                .createPreauthenticatedRequestDetails(details)
                .build()

        val response = objectStorageUtil.client.createPreauthenticatedRequest(request)
        val url = "$baseUrl/${response.preauthenticatedRequest.accessUri}"

        return Image(
            imageId = image.imageId,
            userId = image.userId,
            parId = response.preauthenticatedRequest.id,
            fileName = image.fileName,
            url = url,
            urlExpireTime =
                expireTime.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime(),
        )
    }

    private fun deletePreAuth(parId: String) {
        if (parId.isEmpty()) return

        val request =
            DeletePreauthenticatedRequestRequest.builder()
                .namespaceName(objectStorageProperties.namespaceName)
                .bucketName(objectStorageProperties.bucketName)
                .parId(parId)
                .build()

        objectStorageUtil.client.deletePreauthenticatedRequest(request)
    }

    companion object {
        private const val TWO_HOURS_FOR_SECONDS = 2 * 60 * 60 * 1000
    }
}
