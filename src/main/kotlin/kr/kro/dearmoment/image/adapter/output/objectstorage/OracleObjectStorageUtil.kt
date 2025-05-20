package kr.kro.dearmoment.image.adapter.output.objectstorage

import com.oracle.bmc.ConfigFileReader
import com.oracle.bmc.Region
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.objectstorage.ObjectStorage
import com.oracle.bmc.objectstorage.ObjectStorageClient
import com.oracle.bmc.objectstorage.transfer.UploadConfiguration
import com.oracle.bmc.objectstorage.transfer.UploadManager
import jakarta.annotation.PreDestroy
import org.springframework.stereotype.Component

@Component
class OracleObjectStorageUtil(
    private val objectStorageProperties: OracleObjectStorageProperties,
) {
    val client: ObjectStorage by lazy { initializeClient() }
    val uploadManager: UploadManager by lazy { initializeUploadManager(client) }

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

    @PreDestroy
    fun free() {
        client.close()
    }
}
