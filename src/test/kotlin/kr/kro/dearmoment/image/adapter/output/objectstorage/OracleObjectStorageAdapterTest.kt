import io.kotest.core.spec.style.FunSpec
import kr.kro.dearmoment.image.adapter.output.objectstorage.OracleObjectStorageAdapter
import kr.kro.dearmoment.image.adapter.output.objectstorage.OracleObjectStorageProperties
import kr.kro.dearmoment.image.adapter.output.objectstorage.OracleObjectStorageUtil
import org.springframework.mock.web.MockMultipartFile
import java.io.File
import java.nio.file.Files

class OracleObjectStorageAdapterTest : FunSpec({

    lateinit var adapter: OracleObjectStorageAdapter

    beforeSpec {
        // 테스트 환경에서 ObjectStorageProperties를 수동으로 설정
        val objectStorageProperties =
            OracleObjectStorageProperties(
                bucketName = "dear-moment-local",
                namespaceName = "axi7ktcb95py",
                photoImageDir = "photo/",
            )
        val objectStorageUtil = OracleObjectStorageUtil()
        adapter = OracleObjectStorageAdapter(objectStorageProperties, objectStorageUtil)
    }

    test("upload, delete 테스트") {
        // Given
        val imageFile = File("src/test/resources/test-image.jpg") // 테스트용 이미지 파일
        val multipartFile =
            MockMultipartFile(
                "file",
                imageFile.name,
                Files.probeContentType(imageFile.toPath()),
                Files.readAllBytes(imageFile.toPath()),
            )
        val memberId = 123L // 테스트용 회원 ID

        // When
        val result = adapter.upload(multipartFile, memberId)

        // Then
        adapter.delete(result)
    }
})
