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

    private val baseUrl = "https://" + objectStorageProperties.namespaceName +
            ".objectstorage." + Region.AP_CHUNCHEON_1.regionId + ".oci.customer-oci.com"

    companion object {
        private const val ONE_YEAR_FOR_SECONDS = 24L * 60L * 60L * 1000L * 365L
    }

    override fun upload(file: MultipartFile, userId: Long): Image {
        val inputStream = file.inputStream
        val fileDir = "${objectStorageProperties.photoImageDir}$userId"
        val fileName = "$fileDir/${UUID.randomUUID()}"
        val contentType = "img/${file.contentType?.takeLast(3) ?: "JPG"}"

        val putRequest = PutObjectRequest.builder()
            .bucketName(objectStorageProperties.bucketName)
            .namespaceName(objectStorageProperties.namespaceName)
            .objectName(fileName)
            .contentLength(inputStream.available().toLong())
            .contentType(contentType)
            .putObjectBody(inputStream)
            .build()

        val uploadRequest = UploadRequest.builder(inputStream, inputStream.available().toLong())
            .allowOverwrite(true)
            .build(putRequest)

        objectStorageUtil.uploadManager.upload(uploadRequest)
        println("Uploaded file with fileName: $fileName for userId: $userId")

        val image = Image(
            userId = userId,
            fileName = fileName,
        )

        return getImageWithUrl(image)
    }

    override fun uploadAll(commands: List<SaveImageCommand>): List<Image> {
        return commands.map { upload(it.file, it.userId) }
    }

    override fun delete(image: Image) {
        try {
            val request = DeleteObjectRequest.builder()
                .namespaceName(objectStorageProperties.namespaceName)
                .bucketName(objectStorageProperties.bucketName)
                .objectName(image.fileName)
                .build()

            // 사전 인증 삭제 시도
            deletePreAuth(image.parId)
            println("Attempting to delete preauthenticated request for parId: ${image.parId}")

            val client = objectStorageUtil.client
            client.deleteObject(request)
            println("Successfully deleted image with fileName: ${image.fileName}")
        } catch (e: Exception) {
            if (e.message?.contains("이미지를 찾을 수 없습니다") == true) {
                println("Attempted to delete non-existent image with fileName: ${image.fileName}. Exception message: ${e.message}")
            } else {
                println("Error while deleting image with fileName: ${image.fileName}")
                e.printStackTrace()
                throw e
            }
        }
    }

    override fun getImageWithUrl(image: Image): Image {
        // 먼저 기존의 사전 인증 요청을 삭제합니다.
        deletePreAuth(image.parId)
        println("Deleted preauthenticated request for parId: ${image.parId} before generating new URL")

        val expireTime = Date(System.currentTimeMillis() + ONE_YEAR_FOR_SECONDS)

        val details = CreatePreauthenticatedRequestDetails.builder()
            .accessType(AccessType.ObjectReadWrite)
            .objectName(image.fileName)
            .timeExpires(expireTime)
            .name(image.fileName)
            .build()

        val request = CreatePreauthenticatedRequestRequest.builder()
            .namespaceName(objectStorageProperties.namespaceName)
            .bucketName(objectStorageProperties.bucketName)
            .createPreauthenticatedRequestDetails(details)
            .build()

        val response = objectStorageUtil.client.createPreauthenticatedRequest(request)
        val url = "$baseUrl${response.preauthenticatedRequest.accessUri}"
        println("Created new preauthenticated URL for fileName: ${image.fileName}")

        return Image(
            imageId = image.imageId,
            userId = image.userId,
            parId = response.preauthenticatedRequest.id,
            fileName = image.fileName,
            url = url,
            urlExpireTime = expireTime.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime(),
        )
    }

    private fun deletePreAuth(parId: String) {
        if (parId.isEmpty()) {
            println("No preauthenticated request to delete because parId is empty.")
            return
        }

        try {
            val request = DeletePreauthenticatedRequestRequest.builder()
                .namespaceName(objectStorageProperties.namespaceName)
                .bucketName(objectStorageProperties.bucketName)
                .parId(parId)
                .build()

            objectStorageUtil.client.deletePreauthenticatedRequest(request)
            println("Successfully deleted preauthenticated request for parId: $parId")
        } catch (e: Exception) {
            println("Failed to delete preauthenticated request for parId: $parId. Exception: ${e.message}")
            // 예외를 무시하여 계속 진행
        }
    }
}
