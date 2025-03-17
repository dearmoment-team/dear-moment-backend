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
        // 1) 새 파일 업로드
        val newImage = imageService.save(SaveImageCommand(file = newFile, userId = userId))

        // 2) 기존 대표 이미지 삭제
        imageService.delete(currentImage.imageId)

        return newImage
    }

    /**
     * [서브 이미지] 부분 업데이트 (4장 고정)
     *
     * - 인덱스: 0~3만 유효
     * - DELETE 단독 불가 → 반드시 같은 index에서 UPLOAD와 함께 (교체)
     * - KEEP, UPLOAD, DELETE+UPLOAD 조합만 허용
     * - 파일 매핑: (DELETE+UPLOAD) or (UPLOAD) 액션마다 1개씩 소진
     */
    fun processSubImagesPartial(
        currentSubImages: List<Image>,
        finalRequests: List<SubImageFinalRequest>,
        subImageFiles: List<MultipartFile>,
        userId: Long,
    ): List<Image> {
        // 현재 4장(인덱스 0..3) 보유
        if (currentSubImages.size != 4) {
            throw IllegalStateException("서브 이미지는 항상 4장이어야 합니다. 현재=${currentSubImages.size}")
        }

        // 1) 인덱스별로 액션들을 그룹핑 (ex: index=1 -> [DELETE, UPLOAD] 등)
        val requestsByIndex = finalRequests.groupBy { it.index }

        // 2) 최종 결과(인덱스 0..3)
        val resultMap = currentSubImages.mapIndexed { i, img -> i to img }.toMap(mutableMapOf())

        // UPLOAD (또는 DELETE+UPLOAD) 횟수만큼 subImageFiles를 소진
        var filePointer = 0

        requestsByIndex.forEach { (index, actions) ->
            if (index < 0 || index > 3) {
                throw IllegalArgumentException("서브 이미지는 인덱스 0..3 범위만 허용됩니다. (index=$index)")
            }

            val hasDelete = actions.any { it.action == UpdateSubImageAction.DELETE }
            val hasUpload = actions.any { it.action == UpdateSubImageAction.UPLOAD }
            val keepAction = actions.find { it.action == UpdateSubImageAction.KEEP }

            // 1) KEEP only
            if (actions.size == 1 && keepAction != null) {
                // 아무것도 안 함 (기존 유지)
                val requestImgId = keepAction.imageId
                // 검증: 요청의 imageId와 실제 currentSubImages[index]가 같은지
                if (requestImgId != null && requestImgId != resultMap[index]?.imageId) {
                    throw IllegalArgumentException(
                        "KEEP 액션이지만 imageId($requestImgId) != currentImageId(${resultMap[index]?.imageId})"
                    )
                }
            }
            // 2) DELETE + UPLOAD => 교체
            else if (hasDelete && hasUpload) {
                if (filePointer >= subImageFiles.size) {
                    throw IllegalArgumentException("서브 이미지 업로드 파일이 부족합니다. (index=$index)")
                }
                val uploadFile = subImageFiles[filePointer++]

                // 새 이미지 업로드
                val newImg = imageService.save(SaveImageCommand(file = uploadFile, userId = userId))

                // 기존 이미지 삭제
                val oldImg = resultMap[index]!!
                imageService.delete(oldImg.imageId)

                resultMap[index] = newImg
            }
            // 3) DELETE alone -> 금지
            else if (hasDelete && !hasUpload) {
                throw IllegalArgumentException(
                    "DELETE 액션은 단독으로 사용할 수 없습니다. UPLOAD와 함께 사용하세요. (index=$index)"
                )
            }
            // 4) UPLOAD alone => 교체(기존 삭제)
            else if (hasUpload && !hasDelete) {
                if (filePointer >= subImageFiles.size) {
                    throw IllegalArgumentException("서브 이미지 업로드 파일이 부족합니다. (index=$index)")
                }
                val uploadFile = subImageFiles[filePointer++]
                val newImg = imageService.save(SaveImageCommand(file = uploadFile, userId = userId))

                // 기존 이미지 삭제
                val oldImg = resultMap[index]!!
                imageService.delete(oldImg.imageId)

                resultMap[index] = newImg
            }
            // 5) 기타 조합 (예: KEEP + DELETE 등)
            else {
                throw IllegalArgumentException(
                    "서브 이미지 액션 조합이 올바르지 않습니다. (index=$index, actions=$actions)"
                )
            }
        }

        // 항상 index=0..3 각각 Image 존재
        return (0..3).map { idx -> resultMap[idx]!! }
    }

    /**
     * [추가 이미지] 최종 업데이트 (최대 5장)
     * - DELETE 단독 허용
     * - KEEP, UPLOAD 등 자유 (기존 로직 유지)
     * - ***변경점***: finalRequests.size 대신 최종 result.size 로 판단
     */
    fun processAdditionalImagesFinal(
        currentAdditionalImages: List<Image>,
        finalRequests: List<AdditionalImageFinalRequest>,
        additionalImageFiles: List<MultipartFile>?,
        userId: Long,
        maxCount: Int = 5
    ): List<Image> {
        // ***기존에 있던: if (finalRequests.size > maxCount) { ... } 제거***
        val currentMap = currentAdditionalImages.associateBy { it.imageId }
        val result = mutableListOf<Image>()

        // 업로드 요청들
        val uploadRequests = finalRequests.filter { it.action == UpdateAdditionalImageAction.UPLOAD }
        val files = additionalImageFiles.orEmpty()
        if (uploadRequests.size != files.size) {
            throw IllegalArgumentException(
                "추가 이미지(UPLOAD) 요청 수(${uploadRequests.size})와 업로드된 파일 수(${files.size})가 일치하지 않습니다."
            )
        }

        var fileIndex = 0

        finalRequests.forEach { req ->
            when (req.action) {
                UpdateAdditionalImageAction.KEEP -> {
                    requireNotNull(req.imageId) { "KEEP 액션이면 imageId는 필수입니다." }
                    val existingImg = currentMap[req.imageId]
                        ?: throw IllegalArgumentException("존재하지 않는 추가 이미지 ID: ${req.imageId}")
                    result.add(existingImg)
                }

                UpdateAdditionalImageAction.DELETE -> {
                    requireNotNull(req.imageId) { "DELETE 액션이면 imageId는 필수입니다." }
                    val existingImg = currentMap[req.imageId]
                        ?: throw IllegalArgumentException("존재하지 않는 추가 이미지 ID: ${req.imageId}")

                    // DB/스토리지에서 삭제
                    imageService.delete(existingImg.imageId)
                    // 결과 목록에는 추가 안 함
                }

                UpdateAdditionalImageAction.UPLOAD -> {
                    val file = files[fileIndex++]
                    val newImg = imageService.save(SaveImageCommand(file = file, userId = userId))
                    // 교체 개념이 필요하다면, oldId를 삭제해도 됨:
                    // req.imageId?.let { oldId ->
                    //    currentMap[oldId]?.let { oldImg -> imageService.delete(oldImg.imageId) }
                    // }
                    result.add(newImg)
                }
            }
        }

        // 최종 개수 체크: 5장 초과 시 예외
        if (result.size > maxCount) {
            throw IllegalArgumentException(
                "추가 이미지는 최대 $maxCount 장까지만 가능합니다. (현재=${result.size}장)"
            )
        }

        return result
    }
}
