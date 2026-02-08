package com.moveoftoday.walkorwait.pet

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 펫 렌더링 표준 상수
 *
 * ## 기준점 철학
 * - **pet name이 기준점**
 * - pet name 위치는 절대 변하지 않음
 * - 펫, 아이템은 pet name 기준으로 계산
 *
 * ## 좌표 계산 공식
 * ```
 * nameY = displayHeight - PET_NAME_BOTTOM_MARGIN - NAME_TEXT_HEIGHT
 * petY = nameY - PET_TO_NAME_SPACING - petSize + petType.getDisplayYOffsetDp()
 * ```
 */
object PetDisplayConstants {
    /**
     * pet name 위치 (display 하단에서의 거리)
     * 이 값이 모든 화면에서 pet name의 절대 위치를 결정
     */
    val PET_NAME_BOTTOM_MARGIN = 10.dp

    /**
     * 펫과 이름 간의 간격
     * 펫 스프라이트 하단 ~ pet name 상단 사이 여백
     * 음수 = 이름이 펫 위로 올라감 (겹침)
     */
    val PET_TO_NAME_SPACING = (-43).dp

    /**
     * pet name 텍스트 예상 높이
     * 칭호 + 이름 포함 (13sp ≈ 16dp)
     */
    val NAME_TEXT_HEIGHT = 16.dp

    /**
     * pet name Y 좌표 계산
     * @param displayHeight 디스플레이 전체 높이
     * @return pet name의 Y 좌표 (상단 기준)
     */
    fun calculateNameY(displayHeight: Dp): Dp {
        return displayHeight - PET_NAME_BOTTOM_MARGIN - NAME_TEXT_HEIGHT
    }

    /**
     * 펫 스프라이트 Y 좌표 계산
     * @param displayHeight 디스플레이 전체 높이
     * @param petSize 펫 스프라이트 크기
     * @param petYOffset 펫별 Y offset (getDisplayYOffsetDp)
     * @return 펫의 Y 좌표 (상단 기준)
     */
    fun calculatePetY(
        displayHeight: Dp,
        petSize: Dp,
        petYOffset: Float
    ): Dp {
        val nameY = calculateNameY(displayHeight)
        return nameY - PET_TO_NAME_SPACING - petSize + petYOffset.dp
    }
}
