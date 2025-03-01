package kr.kro.dearmoment.image.adapter.output.objectstorage

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
data class OracleObjectStorageProperties(
    @Value("\${object-storage.namespace-name}")
    val namespaceName: String,
    @Value("\${object-storage.bucket-name}")
    val bucketName: String,
    @Value("\${object-storage.photo-image-dir}")
    val photoImageDir: String,
    @Value("\${object-storage.config-path}")
    val configPath: String,
)
