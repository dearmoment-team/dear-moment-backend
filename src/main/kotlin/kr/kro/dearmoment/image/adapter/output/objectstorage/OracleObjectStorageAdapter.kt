package kr.kro.dearmoment.image.adapter.output.objectstorage

import com.oracle.bmc.Region
import com.oracle.bmc.objectstorage.ObjectStorage
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails
import com.oracle.bmc.objectstorage.model.CreatePreauthenticatedRequestDetails.AccessType
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest
import com.oracle.bmc.objectstorage.requests.DeletePreauthenticatedRequestRequest
import com.oracle.bmc.objectstorage.requests.PutObjectRequest
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadRequest
import io.viascom.nanoid.NanoId
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.application.port.output.GetImageFromObjectStoragePort
import kr.kro.dearmoment.image.application.port.output.UploadImagePort
import kr.kro.dearmoment.image.domain.Image
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedInputStream
import java.time.ZoneId
import java.util.Date
import java.util.UUID

@Component
class OracleObjectStorageAdapter(
    private val objectStorageProperties: OracleObjectStorageProperties,
    private val objectStorageUtil: OracleObjectStorageUtil,
) : UploadImagePort, GetImageFromObjectStoragePort {
    private val baseUrl =
        "https://" + objectStorageProperties.namespaceName +
            ".objectstorage." + Region.AP_CHUNCHEON_1.regionId + ".oci.customer-oci.com"

    override fun upload(
        file: MultipartFile,
        userId: UUID,
    ): Image {
        val fileDir = "${objectStorageProperties.photoImageDir}$userId"
        val extension = convertFileExtension(file.contentType)
        val fileName = "$fileDir/${NanoId.generate()}.$extension"
        val contentType = "img/${file.contentType?.takeLast(3) ?: "JPG"}"
        val inputStream = BufferedInputStream(file.inputStream)
        val fileSize = file.size

        val putRequest =
            PutObjectRequest.builder()
                .bucketName(objectStorageProperties.bucketName)
                .namespaceName(objectStorageProperties.namespaceName)
                .objectName(fileName)
                .contentLength(fileSize)
                .contentType(contentType)
                .putObjectBody(inputStream)
                .build()

        val uploadRequest =
            UploadRequest.builder(inputStream, fileSize)
                .allowOverwrite(true)
                .build(putRequest)

        val uploadManager = objectStorageUtil.uploadManager

        uploadManager.upload(uploadRequest)

        val image =
            Image(
                userId = userId,
                fileName = fileName,
            )

        val client = objectStorageUtil.client
        val imageWithUrl = getImageWithUrl(image, client)

        return imageWithUrl
    }

    override fun uploadAll(commands: List<SaveImageCommand>): List<Image> {
        return commands.map { upload(it.file, it.userId) }
    }

    override fun getImageWithUrl(image: Image): Image {
        val client = objectStorageUtil.client

        deletePreAuth(image.parId, client)

        val expireTime = Date(System.currentTimeMillis() + ONE_YEAR_FOR_SECONDS)

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

        val response =
            client.createPreauthenticatedRequest(request)
                ?: throw CustomException(ErrorCode.IMAGE_NOT_FOUND)

        val url = "$baseUrl${response.preauthenticatedRequest.accessUri}"

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

    fun delete(
        parId: String,
        fileName: String,
    ) {
        val client = objectStorageUtil.client

        deletePreAuth(parId, client)

        val request =
            DeleteObjectRequest.builder()
                .namespaceName(objectStorageProperties.namespaceName)
                .bucketName(objectStorageProperties.bucketName)
                .objectName(fileName)
                .build()

        client.deleteObject(request)
    }

    private fun getImageWithUrl(
        image: Image,
        client: ObjectStorage
    ): Image {
        val expireTime = Date(System.currentTimeMillis() + ONE_YEAR_FOR_SECONDS)

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

        val response =
            client.createPreauthenticatedRequest(request)
                ?: throw CustomException(ErrorCode.IMAGE_NOT_FOUND)

        val url = "$baseUrl${response.preauthenticatedRequest.accessUri}"

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

    private fun deletePreAuth(
        parId: String,
        client: ObjectStorage
    ) {
        if (parId.isEmpty()) return

        val request =
            DeletePreauthenticatedRequestRequest.builder()
                .namespaceName(objectStorageProperties.namespaceName)
                .bucketName(objectStorageProperties.bucketName)
                .parId(parId)
                .build()

        client.deletePreauthenticatedRequest(request)
    }

    private fun convertFileExtension(contentType: String?): String {
        return when (contentType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            else -> "jpg"
        }
    }

    companion object {
        private const val ONE_YEAR_FOR_SECONDS = 24L * 60L * 60L * 1000L * 365L
    }
}
