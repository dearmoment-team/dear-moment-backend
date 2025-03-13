package kr.kro.dearmoment.image.application.handler

import kr.kro.dearmoment.image.application.command.SaveImageCommand
import kr.kro.dearmoment.image.application.service.ImageService
import kr.kro.dearmoment.image.domain.Image
import kr.kro.dearmoment.product.application.dto.request.AdditionalImageFinalRequest
import kr.kro.dearmoment.product.application.dto.request.SubImageFinalRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateAdditionalImageAction
import kr.kro.dearmoment.product.application.dto.request.UpdateProductRequest
import kr.kro.dearmoment.product.application.dto.request.UpdateSubImageAction
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

@Component
class ImageHandler(
    private val imageService: ImageService,
) {
    /**
     * [상품 업데이트] 시, 컨트롤러에서 받은 rawRequest + MultipartFile들을
     * 하나로 합쳐서 최종 UpdateProductRequest로 만들어 줍니다.
     *
     * - productId도 덮어씌움
     * - mainImageFile 설정
     * - subImagesFinal / additionalImagesFinal 중 action=UPLOAD 항목에 실제 파일 매핑
     */
    fun mergeUpdateRequest(
        productId: Long,
        rawRequest: UpdateProductRequest,
        mainImageFile: MultipartFile?,
        subImageFiles: List<MultipartFile>?,
        additionalImageFiles: List<MultipartFile>?,
    ): UpdateProductRequest {
        return rawRequest.copy(
            productId = productId,
            mainImageFile = mainImageFile,
            subImagesFinal = mergeSubImageFiles(rawRequest.subImagesFinal, subImageFiles),
            additionalImagesFinal = mergeAdditionalImageFiles(rawRequest.additionalImagesFinal, additionalImageFiles),
        )
    }

    /**
     * action=UPLOAD인 subImagesFinal 항목과 subImageFiles(List<MultipartFile>)를 1:1 매핑
     */
    private fun mergeSubImageFiles(
        subImagesFinal: List<SubImageFinalRequest>,
        subImageFiles: List<MultipartFile>?,
    ): List<SubImageFinalRequest> {
        if (subImagesFinal.isEmpty()) return emptyList()

        // action=UPLOAD인 항목만 필터링
        val uploadItems = subImagesFinal.filter { it.action == UpdateSubImageAction.UPLOAD }
        val files = subImageFiles.orEmpty()

        if (uploadItems.size != files.size) {
            throw IllegalArgumentException(
                "서브 이미지(UPLOAD) 항목 개수(${uploadItems.size})와 업로드된 파일 개수(${files.size})가 다릅니다.",
            )
        }

        var fileIndex = 0
        return subImagesFinal.map { item ->
            if (item.action == UpdateSubImageAction.UPLOAD) {
                item.copy(newFile = files[fileIndex++])
            } else {
                item
            }
        }
    }

    /**
     * action=UPLOAD인 additionalImagesFinal 항목과 additionalImageFiles(List<MultipartFile>)를 1:1 매핑
     */
    private fun mergeAdditionalImageFiles(
        additionalImagesFinal: List<AdditionalImageFinalRequest>,
        additionalImageFiles: List<MultipartFile>?,
    ): List<AdditionalImageFinalRequest> {
        if (additionalImagesFinal.isEmpty()) return emptyList()

        val uploadItems = additionalImagesFinal.filter { it.action == UpdateAdditionalImageAction.UPLOAD }
        val files = additionalImageFiles.orEmpty()

        if (uploadItems.size != files.size) {
            throw IllegalArgumentException(
                "추가 이미지(UPLOAD) 항목 개수(${uploadItems.size})와 업로드된 파일 개수(${files.size})가 다릅니다.",
            )
        }

        var fileIndex = 0
        return additionalImagesFinal.map { item ->
            if (item.action == UpdateAdditionalImageAction.UPLOAD) {
                item.copy(newFile = files[fileIndex++])
            } else {
                item
            }
        }
    }

    /**
     * 메인 이미지 교체
     * - 새 파일 있으면 새 업로드 후 기존 삭제
     * - 새 파일이 null이면 기존 이미지를 그대로 사용
     */
    fun updateMainImage(
        newFile: MultipartFile,
        userId: Long,
        currentImage: Image,
    ): Image {
        // 새 파일 업로드
        val newImage = imageService.save(SaveImageCommand(file = newFile, userId = userId))
        // 기존 이미지 삭제
        imageService.delete(currentImage.imageId)
        return newImage
    }

    /**
     * 서브 이미지 최종 처리 (정확히 4장)
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
                    requireNotNull(req.imageId) { "KEEP 액션일 경우 imageId는 필수입니다." }
                    val existingImg =
                        currentMap[req.imageId]
                            ?: throw IllegalArgumentException("존재하지 않는 subImage ID: ${req.imageId}")
                    result.add(existingImg)
                }
                UpdateSubImageAction.DELETE -> {
                    requireNotNull(req.imageId) { "DELETE 액션일 경우 imageId는 필수입니다." }
                    val existingImg =
                        currentMap[req.imageId]
                            ?: throw IllegalArgumentException("존재하지 않는 subImage ID: ${req.imageId}")
                    imageService.delete(existingImg.imageId)
                    // DELETE면 결과 목록에 추가 X
                }
                UpdateSubImageAction.UPLOAD -> {
                    requireNotNull(req.newFile) { "UPLOAD 액션일 경우 newFile은 필수입니다." }
                    val newImg = imageService.save(SaveImageCommand(file = req.newFile, userId = userId))
                    // 기존 imageId가 있었다면 교체이므로 삭제
                    req.imageId?.let { oldId ->
                        currentMap[oldId]?.let { oldImg -> imageService.delete(oldImg.imageId) }
                    }
                    result.add(newImg)
                }
            }
        }

        return result
    }

    /**
     * 추가 이미지 최종 처리 (0~5장)
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
                    // DELETE는 결과 목록에 추가 X
                }
                UpdateAdditionalImageAction.UPLOAD -> {
                    requireNotNull(req.newFile) { "UPLOAD 액션일 경우 newFile은 필수입니다." }
                    val newImg = imageService.save(SaveImageCommand(file = req.newFile, userId = userId))
                    // 기존 이미지가 있다면 삭제
                    req.imageId?.let { oldId ->
                        currentMap[oldId]?.let { oldImg -> imageService.delete(oldImg.imageId) }
                    }
                    result.add(newImg)
                }
            }
        }

        return result
    }
}
