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
    fun uploadObject(supplier: () -> UploadRequest) =
        runCatching {
            val client = initializeClient()
            val uploadManager = initializeUploadManager(client)
            val request = supplier()

            uploadManager.upload(request)

            client.close()
        }.onFailure { throw it }

    fun getObject(supplier: () -> CreatePreauthenticatedRequestRequest): CreatePreauthenticatedRequestResponse =
        runCatching {
            val client = initializeClient()
            val request = supplier()
            val response = client.createPreauthenticatedRequest(request)
            response ?: throw CustomException(ErrorCode.IMAGE_NOT_FOUND)
        }.getOrElse {
            throw it
        }

    fun deleteObject(supplier: () -> DeleteObjectRequest) =
        runCatching {
            val client = initializeClient()
            val request = supplier()

            client.deleteObject(request)
            client.close()
        }.onFailure { throw it }

    fun deletePreAuth(supplier: () -> DeletePreauthenticatedRequestRequest) =
        runCatching {
            val client = initializeClient()
            val request = supplier()

            client.deletePreauthenticatedRequest(request)
            client.close()
        }.onFailure { throw it }

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
