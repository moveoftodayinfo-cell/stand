package com.moveoftoday.walkorwait

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.util.Log
import android.widget.RemoteViews
import com.moveoftoday.walkorwait.pet.PetType
import com.moveoftoday.walkorwait.pet.PetTypeV2

class FastingWidgetProvider : AppWidgetProvider() {
    private val TAG = "FastingWidgetProvider"

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        Log.d(TAG, "onUpdate called for ${appWidgetIds.size} widgets")
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        Log.d(TAG, "Widget enabled")
    }

    override fun onDisabled(context: Context) {
        Log.d(TAG, "Widget disabled")
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, FastingWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val ids = AppWidgetManager.getInstance(context)
                .getAppWidgetIds(ComponentName(context, FastingWidgetProvider::class.java))
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }

        private fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_fasting)
            val prefs = PreferenceManager(context)

            // 펫 이미지 로드 (V2 우선, 없으면 V1)
            val petTypeV2 = prefs.getPetTypeV2()
            val petBitmap = if (petTypeV2 != null) {
                loadPetV2FirstFrame(context, petTypeV2, prefs)
            } else {
                val petTypeName = prefs.getPetType()
                val petType = petTypeName?.let {
                    try { PetType.valueOf(it) } catch (e: Exception) { null }
                }
                if (petType != null) loadPetFirstFrame(context, petType) else null
            }

            if (petBitmap != null) {
                views.setImageViewBitmap(R.id.pet_image, petBitmap)
            }

            // ChallengeManager에서 백그라운드 챌린지 확인
            val challengeManager = ChallengeManager.getInstance(context)
            val backgroundProgress = challengeManager.backgroundProgress.value

            // 간헐적 단식이 진행 중인지 확인
            if (backgroundProgress != null &&
                backgroundProgress.challenge.isBackgroundTimer &&
                backgroundProgress.status == ChallengeStatus.RUNNING) {

                // 경과 시간 계산
                val elapsedMillis = System.currentTimeMillis() - backgroundProgress.startTime
                val elapsedHours = (elapsedMillis / (1000 * 60 * 60)).toInt()
                val elapsedMinutes = ((elapsedMillis / (1000 * 60)) % 60).toInt()

                // 남은 시간 계산
                val remainingSeconds = backgroundProgress.remainingSeconds
                val remainingHours = (remainingSeconds / 3600).toInt()
                val remainingMinutes = ((remainingSeconds % 3600) / 60).toInt()

                // 타이머 텍스트 (왼쪽)
                views.setTextViewText(R.id.timer_elapsed, getElapsedText(elapsedHours, elapsedMinutes))
                views.setTextViewText(R.id.timer_remaining, getRemainingText(remainingHours, remainingMinutes))

                // 진행률 바
                val progress = (backgroundProgress.progressPercent * 100).toInt()
                views.setProgressBar(R.id.progress_bar, 100, progress, false)

                // 펫 메시지 (오른쪽) - 경과 시간에 따라, 이모지 제거
                val message = getFastingStageMessage(elapsedHours)
                views.setTextViewText(R.id.pet_message, message)

                // 챌린지 이름
                views.setTextViewText(R.id.challenge_name, backgroundProgress.challenge.name)
            } else {
                // 진행 중인 단식 없음
                views.setTextViewText(R.id.timer_elapsed, getNoFastingText())
                views.setTextViewText(R.id.timer_remaining, "")
                views.setProgressBar(R.id.progress_bar, 100, 0, false)
                views.setTextViewText(R.id.pet_message, getStartFastingText())
                views.setTextViewText(R.id.challenge_name, getIntermittentFastingText())
            }

            // 위젯 클릭 시 앱 열기
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getFastingStageMessage(elapsedHours: Int): String {
            val lang = java.util.Locale.getDefault().language
            return when {
                elapsedHours < 4 -> when (lang) {
                    "ko" -> "소화 중!\n혈당 안정화"
                    "ja" -> "消化中!\n血糖安定化"
                    "zh" -> "消化中!\n血糖稳定"
                    "es" -> "¡Digiriendo!\nEstabilizando azúcar"
                    else -> "Digesting!\nStabilizing glucose"
                }
                elapsedHours < 8 -> when (lang) {
                    "ko" -> "지방 연소\n시작!"
                    "ja" -> "脂肪燃焼\n開始!"
                    "zh" -> "开始燃烧\n脂肪!"
                    "es" -> "¡Quemando\ngrasa!"
                    else -> "Fat burning\nstarted!"
                }
                elapsedHours < 12 -> when (lang) {
                    "ko" -> "케톤 생성 중"
                    "ja" -> "ケトン生成中"
                    "zh" -> "生成酮体中"
                    "es" -> "Generando cetonas"
                    else -> "Producing ketones"
                }
                elapsedHours < 16 -> when (lang) {
                    "ko" -> "자가포식\n활성화!"
                    "ja" -> "オートファジー\n活性化!"
                    "zh" -> "自噬\n激活!"
                    "es" -> "¡Autofagia\nactivada!"
                    else -> "Autophagy\nactivated!"
                }
                else -> when (lang) {
                    "ko" -> "성장호르몬\n증가!"
                    "ja" -> "成長ホルモン\n増加!"
                    "zh" -> "生长激素\n增加!"
                    "es" -> "¡Hormona de crecimiento\naumentada!"
                    else -> "Growth hormone\nincreased!"
                }
            }
        }

        private fun getNoFastingText(): String {
            return when (java.util.Locale.getDefault().language) {
                "ko" -> "단식 없음"
                "ja" -> "断食なし"
                "zh" -> "无禁食"
                "es" -> "Sin ayuno"
                else -> "No fasting"
            }
        }

        private fun getStartFastingText(): String {
            return when (java.util.Locale.getDefault().language) {
                "ko" -> "단식 시작해보세요"
                "ja" -> "断食を始めてみよう"
                "zh" -> "开始禁食吧"
                "es" -> "Intenta ayunar"
                else -> "Try starting a fast"
            }
        }

        private fun getIntermittentFastingText(): String {
            return when (java.util.Locale.getDefault().language) {
                "ko" -> "간헐적 단식"
                "ja" -> "間欠的断食"
                "zh" -> "间歇性禁食"
                "es" -> "Ayuno intermitente"
                else -> "Intermittent Fasting"
            }
        }

        private fun getElapsedText(hours: Int, minutes: Int): String {
            return when (java.util.Locale.getDefault().language) {
                "ko" -> String.format("%d:%02d 경과", hours, minutes)
                "ja" -> String.format("%d:%02d 経過", hours, minutes)
                "zh" -> String.format("%d:%02d 已过", hours, minutes)
                "es" -> String.format("%d:%02d pasado", hours, minutes)
                else -> String.format("%d:%02d elapsed", hours, minutes)
            }
        }

        private fun getRemainingText(hours: Int, minutes: Int): String {
            return when (java.util.Locale.getDefault().language) {
                "ko" -> String.format("%d:%02d 남음", hours, minutes)
                "ja" -> String.format("%d:%02d 残り", hours, minutes)
                "zh" -> String.format("%d:%02d 剩余", hours, minutes)
                "es" -> String.format("%d:%02d restante", hours, minutes)
                else -> String.format("%d:%02d remaining", hours, minutes)
            }
        }

        /**
         * V2 펫 첫 프레임 로드
         */
        private fun loadPetV2FirstFrame(context: Context, petType: PetTypeV2, prefs: PreferenceManager): Bitmap? {
            val stage = prefs.getEffectiveDisplayStage()
            val animationType = "idle"

            // 스킨 가져오기
            val skinId = prefs.getPetSkin()
            val petSkin = com.moveoftoday.walkorwait.pet.DefaultSkins.getById(skinId)

            return try {
                val assetPath = "pets/${petType.folderName}/${stage.folderName}/$animationType/frame_000.png"
                context.assets.open(assetPath).use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)?.let { bitmap ->
                        var result = toGrayscale(bitmap)
                        // 스킨 적용 (colorMatrix 방식)
                        petSkin?.colorMatrix?.let { matrix ->
                            result = applyColorMatrix(result, matrix)
                        }
                        result
                    }
                }
            } catch (e: Exception) {
                // 애니메이션 폴더가 없으면 idle로 폴백
                try {
                    val fallbackPath = "pets/${petType.folderName}/${stage.folderName}/idle/frame_000.png"
                    context.assets.open(fallbackPath).use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)?.let { bitmap ->
                            var result = toGrayscale(bitmap)
                            petSkin?.colorMatrix?.let { matrix ->
                                result = applyColorMatrix(result, matrix)
                            }
                            result
                        }
                    }
                } catch (e2: Exception) {
                    null
                }
            }
        }

        /**
         * V1 펫 첫 프레임 로드
         */
        private fun loadPetFirstFrame(context: Context, petType: PetType): Bitmap? {
            val prefs = PreferenceManager(context)
            val skinId = prefs.getPetSkin()
            val petSkin = com.moveoftoday.walkorwait.pet.DefaultSkins.getById(skinId)

            return try {
                val assetPath = petType.idleAssetPath
                context.assets.open(assetPath).use { inputStream ->
                    val spriteSheet = BitmapFactory.decodeStream(inputStream)
                    if (spriteSheet != null) {
                        val frameCount = petType.idleFrames
                        val frameWidth = spriteSheet.width / frameCount
                        val frameHeight = spriteSheet.height
                        val frame = Bitmap.createBitmap(spriteSheet, 0, 0, frameWidth, frameHeight)
                        var result = toGrayscale(frame)
                        // 스킨 적용 (colorMatrix 방식)
                        petSkin?.colorMatrix?.let { matrix ->
                            result = applyColorMatrix(result, matrix)
                        }
                        result
                    } else null
                }
            } catch (e: Exception) {
                null
            }
        }

        /**
         * 그레이스케일 변환
         */
        private fun toGrayscale(original: Bitmap): Bitmap {
            val grayscale = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(grayscale)
            val paint = Paint()
            val colorMatrix = ColorMatrix()
            colorMatrix.setSaturation(0f)
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(original, 0f, 0f, paint)
            return grayscale
        }

        /**
         * ColorMatrix 적용 (스킨 컬러)
         */
        private fun applyColorMatrix(original: Bitmap, matrix: FloatArray): Bitmap {
            val result = Bitmap.createBitmap(original.width, original.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(result)
            val paint = Paint()
            val colorMatrix = ColorMatrix(matrix)
            paint.colorFilter = ColorMatrixColorFilter(colorMatrix)
            canvas.drawBitmap(original, 0f, 0f, paint)
            return result
        }
    }
}
