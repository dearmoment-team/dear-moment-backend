import io.kotest.core.annotation.Tags
import io.kotest.core.spec.style.FunSpec
import kr.kro.dearmoment.DearMomentApplication
import kr.kro.dearmoment.image.adapter.output.objectstorage.OracleObjectStorageAdapter
import kr.kro.dearmoment.image.adapter.output.objectstorage.OracleObjectStorageProperties
import kr.kro.dearmoment.image.adapter.output.objectstorage.OracleObjectStorageUtil
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import java.io.File
import java.nio.file.Files

@Tags("object-storage")
@SpringBootTest(classes = [DearMomentApplication::class])
@ActiveProfiles("test")
class OracleObjectStorageAdapterTest(
    private val objectStorageProperties: OracleObjectStorageProperties,
) : FunSpec({
        test("upload, delete 테스트") {
            val objectStorageUtil = OracleObjectStorageUtil(objectStorageProperties)
            val adapter = OracleObjectStorageAdapter(objectStorageProperties, objectStorageUtil)
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
