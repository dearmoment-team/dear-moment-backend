package kr.kro.dearmoment.image.adapter.output.objectstorage

import com.oracle.bmc.ConfigFileReader
import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.ObjectStorage
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.requests.CreatePreauthenticatedRequestRequest
import com.oracle.bmc.objectstorage.requests.DeleteObjectRequest
import com.oracle.bmc.objectstorage.requests.DeletePreauthenticatedRequestRequest
import com.oracle.bmc.objectstorage.responses.CreatePreauthenticatedRequestResponse
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration
import com.oracle.bmc.objectstorage.transfer.UploadManager
import com.oracle.bmc.objectstorage.transfer.UploadManager.UploadRequest
import kr.kro.dearmoment.common.exception.CustomException
import kr.kro.dearmoment.common.exception.ErrorCode
import org.springframework.stereotype.Component

@Component
class OracleObjectStorageUtil(
    private val objectStorageProperties: OracleObjectStorageProperties,
) {
    fun uploadObject(supplier: () -> UploadRequest) {
        val client = initializeClient()
        val uploadManager = initializeUploadManager(client)

        try {
            val request = supplier()
            uploadManager.upload(request)
        } catch (e: Exception) {
            throw e // 필요하면 로깅 후 다시 던질 수도 있음
        } finally {
            client.close() // 예외 발생 여부와 상관없이 실행됨
        }
    }

    fun getObject(supplier: () -> CreatePreauthenticatedRequestRequest): CreatePreauthenticatedRequestResponse {
        val client = initializeClient()
        try {
            val request = supplier()
            return client.createPreauthenticatedRequest(request) ?: throw CustomException(ErrorCode.IMAGE_NOT_FOUND)
        } finally {
            client.close()
        }
    }

    fun deleteObject(supplier: () -> DeleteObjectRequest) {
        val client = initializeClient()
        try {
            val request = supplier()
            client.deleteObject(request)
        } finally {
            client.close()
        }
    }

    fun deletePreAuth(supplier: () -> DeletePreauthenticatedRequestRequest) {
        val client = initializeClient()
        try {
            val request = supplier()
            client.deletePreauthenticatedRequest(request)
        } finally {
            client.close()
        }
    }

    private fun initializeClient(): ObjectStorage {
        val config = ConfigFileReader.parse(objectStorageProperties.configPath, "DEFAULT")
        val provider = ConfigFileAuthenticationDetailsProvider(config)

        return ObjectStorageClient.builder()
            .region(Region.AP_CHUNCHEON_1)
            .build(provider)
    }

    private fun initializeUploadManager(client: ObjectStorage): UploadManager {
        val config =
            UploadConfiguration.builder()
                .allowMultipartUploads(true)
                .allowParallelUploads(true)
                .build()

        return UploadManager(client, config)
    }
}
