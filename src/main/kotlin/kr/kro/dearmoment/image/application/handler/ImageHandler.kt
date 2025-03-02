package kr.kro.dearmoment.image.application.handler

import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.dto.request.AdditionalImageFinalRequest
import kr.kro.dearmoment.product.application.dto.request.SubImageFinalRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateAdditionalImageAction
import kr.kro.dearmoment.product.application.dto.request.UpdateSubImageAction
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class ImageHandler(
    private val imageService: ImageService,
) {
    /**
     * 메인 이미지 교체
     * - newFile(새 파일)이 있으면 새 업로드 후 기존 삭제
     * - newFile이 null이면 기존 이미지를 그대로 사용
     */
    fun updateMainImage(
        newFile: MultipartFile,
        userId: Long,
        currentImage: Image,
    ): Image {
        // 새 파일 업로드
        val newImage = imageService.uploadSingleImage(newFile, userId)
        // 기존 이미지 삭제
        imageService.delete(currentImage.imageId)
        return newImage
    }

    /**
     * 서브 이미지 최종 처리 (정확히 4장)
     *
     * @param currentSubImages 기존 서브 이미지 리스트 (도메인 객체)
     * @param finalRequests    최종 서브이미지 요청 (4장)
     * @param userId           사용자 ID
     * @return 최종 4장의 Image 목록
     */
    fun processSubImagesFinal(
        currentSubImages: List<Image>,
        finalRequests: List<SubImageFinalRequest>,
        userId: Long,
    ): List<Image> {
        // 1) 서브 이미지는 정확히 4장이어야 한다
        if (finalRequests.size != 4) {
            throw IllegalArgumentException("서브 이미지는 정확히 4장이어야 합니다. 현재 ${finalRequests.size}장입니다.")
        }

        // 기존 이미지들을 imageId로 매핑
        val currentMap = currentSubImages.associateBy { it.imageId }
        val result = mutableListOf<Image>()

        // 2) 요청된 4개 항목을 순회하며, 액션에 따라 처리
        finalRequests.forEach { req ->
            when (req.action) {
                UpdateSubImageAction.KEEP -> {
                    // 기존 이미지 유지
                    requireNotNull(req.imageId) { "KEEP 액션일 경우 imageId는 필수입니다." }
                    val existingImg =
                        currentMap[req.imageId]
                            ?: throw IllegalArgumentException("존재하지 않는 subImage ID: ${req.imageId}")
                    result.add(existingImg)
                }

                UpdateSubImageAction.DELETE -> {
                    // 기존 이미지 삭제
                    requireNotNull(req.imageId) { "DELETE 액션일 경우 imageId는 필수입니다." }
                    val existingImg =
                        currentMap[req.imageId]
                            ?: throw IllegalArgumentException("존재하지 않는 subImage ID: ${req.imageId}")
                    imageService.delete(existingImg.imageId)
                    // DELETE이면 결과 목록에 추가하지 않음
                }

                UpdateSubImageAction.UPLOAD -> {
                    // 새 이미지 업로드
                    requireNotNull(req.newFile) { "UPLOAD 액션일 경우 newFile은 필수입니다." }
                    val newImg = imageService.uploadSingleImage(req.newFile, userId)
                    // 만약 교체 개념이라면, 기존 imageId가 있다면 삭제
                    req.imageId?.let { oldId ->
                        currentMap[oldId]?.let { oldImg ->
                            imageService.delete(oldImg.imageId)
                        }
                    }
                    result.add(newImg)
                }
            }
        }

        // 3) 이미 DELETE나 UPLOAD로 제거된 이미지가 아닌데,
        //    result에 포함되지 않는 기존 이미지가 있다면 추가 삭제 로직을 넣을 수 있음.
        //    필요 시 로직 구현. (현재는 액션이 명시되지 않으면 그대로 두는 것으로 가정)

        return result
    }

    /**
     * 추가 이미지 최종 처리 (0~5장)
     *
     * @param currentAdditionalImages 기존 추가 이미지 리스트 (도메인 객체)
     * @param finalRequests           최종 추가이미지 요청 (최대 5장)
     * @param userId                  사용자 ID
     * @param maxCount                최대 추가 이미지 개수 (기본 5)
     * @return 최종 추가 이미지 목록
     */
    fun processAdditionalImagesFinal(
        currentAdditionalImages: List<Image>,
        finalRequests: List<AdditionalImageFinalRequest>,
        userId: Long,
        maxCount: Int = 5,
    ): List<Image> {
        // 1) 추가 이미지는 최대 maxCount장까지만 허용
        if (finalRequests.size > maxCount) {
            throw IllegalArgumentException("추가 이미지는 최대 $maxCount 장까지만 가능합니다. 현재 ${finalRequests.size}장입니다.")
        }

        val currentMap = currentAdditionalImages.associateBy { it.imageId }
        val result = mutableListOf<Image>()

        // 2) 요청된 각 항목에 대해 액션별 처리
        finalRequests.forEach { req ->
            when (req.action) {
                UpdateAdditionalImageAction.KEEP -> {
                    requireNotNull(req.imageId) { "KEEP 액션일 경우 imageId는 필수입니다." }
                    val existingImg =
                        currentMap[req.imageId]
                            ?: throw IllegalArgumentException("존재하지 않는 additionalImage ID: ${req.imageId}")
                    result.add(existingImg)
                }

                UpdateAdditionalImageAction.DELETE -> {
                    requireNotNull(req.imageId) { "DELETE 액션일 경우 imageId는 필수입니다." }
                    val existingImg =
                        currentMap[req.imageId]
                            ?: throw IllegalArgumentException("존재하지 않는 additionalImage ID: ${req.imageId}")
                    imageService.delete(existingImg.imageId)
                    // 결과 목록에는 추가하지 않음
                }

                UpdateAdditionalImageAction.UPLOAD -> {
                    requireNotNull(req.newFile) { "UPLOAD 액션일 경우 newFile은 필수입니다." }
                    val newImg = imageService.uploadSingleImage(req.newFile, userId)
                    // 기존에 있던 이미지 교체라면 삭제
                    req.imageId?.let { oldId ->
                        currentMap[oldId]?.let { oldImg ->
                            imageService.delete(oldImg.imageId)
                        }
                    }
                    result.add(newImg)
                }
            }
        }

        // 3) 위 서브 이미지와 동일하게,
        //    DELETE/UPLOAD로 제거되지 않았지만 result에 없는 기존 이미지를
        //    별도로 삭제할지 여부는 요구사항에 따라 결정.

        return result
    }
}
