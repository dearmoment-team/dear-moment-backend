package kr.kro.dearmoment.image.adapter.output.objectstorage

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "object-storage")
data class OracleObjectStorageProperties
    @ConstructorBinding
    constructor(
        val bucketName: String,
        val namespaceName: String,
        val photoImageDir: String,
    )
