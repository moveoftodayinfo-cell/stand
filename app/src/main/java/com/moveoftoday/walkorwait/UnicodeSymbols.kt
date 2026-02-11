package com.moveoftoday.walkorwait

/**
 * Unicode 텍스트 심볼 상수
 *
 * \uFE0E (Unicode Variation Selector-15)를 사용하여
 * 이모지를 모노크롬 텍스트 스타일로 강제 렌더링합니다.
 *
 * 예:
 * - ☀ 그냥 쓰면 → 🌞 (컬러 이모지)
 * - ☀\uFE0E 쓰면 → ☀ (모노크롬 텍스트)
 */
object UnicodeSymbols {
    private const val TEXT_SELECTOR = "\uFE0E" // 텍스트 스타일 강제

    // 날씨
    const val SUN = "☀$TEXT_SELECTOR"
    const val MOON = "☾$TEXT_SELECTOR"
    const val CLOUD = "☁$TEXT_SELECTOR"
    const val PARTLY_CLOUDY = "⛅$TEXT_SELECTOR"
    const val UMBRELLA = "☂$TEXT_SELECTOR"
    const val HEAVY_RAIN = "☔$TEXT_SELECTOR"
    const val SNOWFLAKE = "❄$TEXT_SELECTOR"
    const val LIGHTNING = "⚡$TEXT_SELECTOR"

    // 감정/상태
    const val SPARKLES = "✨$TEXT_SELECTOR"
    const val STAR = "⭐$TEXT_SELECTOR"
    const val STAR_OUTLINE = "☆$TEXT_SELECTOR"
    const val HEART = "❤$TEXT_SELECTOR"
    const val HEART_OUTLINE = "♡"
    const val HEART_FILLED = "♥$TEXT_SELECTOR"

    // 동작/상태
    const val CHECK = "✓"
    const val CROSS = "✗"
    const val CHECKMARK = "✅$TEXT_SELECTOR"
    const val CROSSMARK = "❌$TEXT_SELECTOR"
    const val WARNING = "⚠$TEXT_SELECTOR"

    // 방향
    const val UP_ARROW = "↑"
    const val DOWN_ARROW = "↓"
    const val LEFT_ARROW = "←"
    const val RIGHT_ARROW = "→"
    const val TRIANGLE_UP = "△"
    const val TRIANGLE_DOWN = "▽"

    // 도형
    const val CIRCLE_FILLED = "●"
    const val CIRCLE_OUTLINE = "○"
    const val SQUARE_FILLED = "■"
    const val SQUARE_OUTLINE = "□"
    const val DIAMOND = "◈"
    const val DIAMOND_OUTLINE = "◇"

    // 기타
    const val GEAR = "⚙"
    const val CLOCK = "⏰$TEXT_SELECTOR"
    const val STOPWATCH = "⏱$TEXT_SELECTOR"
    const val HOURGLASS = "⌛$TEXT_SELECTOR"
    const val MUSIC_NOTE = "♪"
    const val MUSIC_NOTES = "♫"
    const val EQUALS = "≡"

    // 운동/건강
    const val FIRE = "🔥$TEXT_SELECTOR"
    const val MUSCLE = "💪$TEXT_SELECTOR"
    const val TROPHY = "🏆$TEXT_SELECTOR"
    const val TARGET = "🎯$TEXT_SELECTOR"
    const val HUNDRED = "💯$TEXT_SELECTOR"

    // 축하
    const val PARTY_POPPER = "🎉$TEXT_SELECTOR"
    const val CONFETTI = "🎊$TEXT_SELECTOR"
    const val CLAP = "👏$TEXT_SELECTOR"
    const val RAINBOW = "🌈$TEXT_SELECTOR"
}
