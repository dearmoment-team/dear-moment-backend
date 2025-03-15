package kr.kro.dearmoment.image.application.handler

import kr.kro.dearmoment.image.application.command.SaveImageCommand
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
     * - 새 파일이 있으면 업로드 후 기존 이미지를 삭제하고 새 이미지 반환
     * - 새 파일이 없으면 기존 이미지를 그대로 사용
     */
    fun updateMainImage(
        newFile: MultipartFile,
        userId: Long,
        currentImage: Image,
    ): Image {
        val newImage = imageService.save(SaveImageCommand(file = newFile, userId = userId))
        imageService.delete(currentImage.imageId)
        return newImage
    }

    /**
     * 서브 이미지 최종 처리 (정확히 4장)
     *
     * @param currentSubImages 현재 DB에 저장된 서브 이미지 목록
     * @param finalRequests 요청된 서브 이미지 항목 (액션 및 기존 이미지 ID 정보만 포함)
     * @param subImageFiles 업로드된 서브 이미지 파일 목록 (UPLOAD 액션에 해당하는 파일들)
     * @param userId 요청자 ID
     */
    fun processSubImagesFinal(
        currentSubImages: List<Image>,
        finalRequests: List<SubImageFinalRequest>,
        subImageFiles: List<MultipartFile>,
        userId: Long,
    ): List<Image> {
        if (finalRequests.size != 4) {
            throw IllegalArgumentException("서브 이미지는 정확히 4장이어야 합니다. 현재 ${finalRequests.size}장입니다.")
        }
        val currentMap = currentSubImages.associateBy { it.imageId }
        val result = mutableListOf<Image>()

        // UPLOAD 액션인 요청과 업로드된 파일 수가 일치하는지 확인
        val uploadRequests = finalRequests.filter { it.action == UpdateSubImageAction.UPLOAD }
        if (uploadRequests.size != subImageFiles.size) {
            throw IllegalArgumentException("서브 이미지(UPLOAD) 요청 수(${uploadRequests.size})와 업로드된 파일 수(${subImageFiles.size})가 일치하지 않습니다.")
        }
        var fileIndex = 0

        finalRequests.forEach { req ->
            when (req.action) {
                UpdateSubImageAction.KEEP -> {
                    requireNotNull(req.imageId) { "KEEP 액션일 경우 imageId는 필수입니다." }
                    val existingImg =
                        currentMap[req.imageId]
                            ?: throw IllegalArgumentException("존재하지 않는 서브 이미지 ID: ${req.imageId}")
                    result.add(existingImg)
                }
                UpdateSubImageAction.DELETE -> {
                    requireNotNull(req.imageId) { "DELETE 액션일 경우 imageId는 필수입니다." }
                    val existingImg =
                        currentMap[req.imageId]
                            ?: throw IllegalArgumentException("존재하지 않는 서브 이미지 ID: ${req.imageId}")
                    imageService.delete(existingImg.imageId)
                    // 삭제 시 결과 목록에 추가하지 않음
                }
                UpdateSubImageAction.UPLOAD -> {
                    val file = subImageFiles[fileIndex++]
                    val newImg = imageService.save(SaveImageCommand(file = file, userId = userId))
                    // 기존 이미지가 있었다면 삭제 (교체)
                    req.imageId?.let { oldId ->
                        currentMap[oldId]?.let { oldImg ->
                            imageService.delete(oldImg.imageId)
                        }
                    }
                    result.add(newImg)
                }
            }
        }
        return result
    }

    /**
     * 추가 이미지 최종 처리 (최대 5장)
     *
     * @param currentAdditionalImages 현재 DB에 저장된 추가 이미지 목록
     * @param finalRequests 요청된 추가 이미지 항목 (액션 및 기존 이미지 ID 정보만 포함)
     * @param additionalImageFiles 업로드된 추가 이미지 파일 목록 (UPLOAD 액션에 해당하는 파일들)
     * @param userId 요청자 ID
     * @param maxCount 최대 허용 이미지 개수 (기본값 5)
     */
    fun processAdditionalImagesFinal(
        currentAdditionalImages: List<Image>,
        finalRequests: List<AdditionalImageFinalRequest>,
        additionalImageFiles: List<MultipartFile>?,
        userId: Long,
        maxCount: Int = 5,
    ): List<Image> {
        if (finalRequests.size > maxCount) {
            throw IllegalArgumentException("추가 이미지는 최대 $maxCount 장까지만 가능합니다. 현재 ${finalRequests.size}장입니다.")
        }
        val currentMap = currentAdditionalImages.associateBy { it.imageId }
        val result = mutableListOf<Image>()

        val uploadRequests = finalRequests.filter { it.action == UpdateAdditionalImageAction.UPLOAD }
        val files = additionalImageFiles.orEmpty()
        if (uploadRequests.size != files.size) {
            throw IllegalArgumentException("추가 이미지(UPLOAD) 요청 수(${uploadRequests.size})와 업로드된 파일 수(${files.size})가 일치하지 않습니다.")
        }
        var fileIndex = 0

        finalRequests.forEach { req ->
            when (req.action) {
                UpdateAdditionalImageAction.KEEP -> {
                    requireNotNull(req.imageId) { "KEEP 액션일 경우 imageId는 필수입니다." }
                    val existingImg =
                        currentMap[req.imageId]
                            ?: throw IllegalArgumentException("존재하지 않는 추가 이미지 ID: ${req.imageId}")
                    result.add(existingImg)
                }
                UpdateAdditionalImageAction.DELETE -> {
                    requireNotNull(req.imageId) { "DELETE 액션일 경우 imageId는 필수입니다." }
                    val existingImg =
                        currentMap[req.imageId]
                            ?: throw IllegalArgumentException("존재하지 않는 추가 이미지 ID: ${req.imageId}")
                    imageService.delete(existingImg.imageId)
                    // 삭제 시 결과 목록에 추가하지 않음
                }
                UpdateAdditionalImageAction.UPLOAD -> {
                    val file = files[fileIndex++]
                    val newImg = imageService.save(SaveImageCommand(file = file, userId = userId))
                    req.imageId?.let { oldId ->
                        currentMap[oldId]?.let { oldImg ->
                            imageService.delete(oldImg.imageId)
                        }
                    }
                    result.add(newImg)
                }
            }
        }
        return result
    }
}
