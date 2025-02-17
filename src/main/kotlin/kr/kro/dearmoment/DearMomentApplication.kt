package kr.kro.dearmoment

import kr.kro.dearmoment.image.adapter.output.objectstorage.OracleObjectStorageProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(OracleObjectStorageProperties::class)
class DearMomentApplication

fun main(args: Array<String>) {
    runApplication<DearMomentApplication>(*args)
}
