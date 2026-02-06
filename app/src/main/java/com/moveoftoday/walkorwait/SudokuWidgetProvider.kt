package com.moveoftoday.walkorwait

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.widget.RemoteViews
import com.moveoftoday.walkorwait.pet.PetType
import com.moveoftoday.walkorwait.pet.PetTypeV2
import com.moveoftoday.walkorwait.pet.PetAnimationTypeV2

/**
 * 스도쿠 위젯 - 간단한 4x4 스도쿠 퍼즐
 * 위젯에서 바로 플레이 가능
 */
class SudokuWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val appWidgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return

        when (intent.action) {
            ACTION_NEW_PUZZLE -> {
                // 새 퍼즐 생성
                val prefs = PreferenceManager(context)
                val newPuzzleIndex = (prefs.getSudokuPuzzleIndex() + 1) % PUZZLES.size
                prefs.setSudokuPuzzleIndex(newPuzzleIndex)
                prefs.clearSudokuProgress(appWidgetId)
                updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
            }
            ACTION_CELL_CLICK -> {
                // 셀 선택
                val row = intent.getIntExtra("row", -1)
                val col = intent.getIntExtra("col", -1)
                if (row >= 0 && col >= 0) {
                    val prefs = PreferenceManager(context)
                    prefs.setSudokuSelectedCell(appWidgetId, row, col)
                    updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                }
            }
            ACTION_NUMBER_CLICK -> {
                // 숫자 입력
                val number = intent.getIntExtra("number", -1)
                if (number > 0) {
                    val prefs = PreferenceManager(context)
                    val selected = prefs.getSudokuSelectedCell(appWidgetId)
                    if (selected.first >= 0 && selected.second >= 0) {
                        // 해당 셀이 빈 칸인지 확인
                        val puzzleIndex = prefs.getSudokuPuzzleIndex() % PUZZLES.size
                        val puzzle = PUZZLES[puzzleIndex]
                        if (puzzle.initial[selected.first][selected.second] == 0) {
                            prefs.setSudokuCellValue(appWidgetId, selected.first, selected.second, number)
                        }
                        updateWidget(context, AppWidgetManager.getInstance(context), appWidgetId)
                    }
                }
            }
        }
    }

    companion object {
        private const val ACTION_NEW_PUZZLE = "com.moveoftoday.walkorwait.SUDOKU_NEW"
        private const val ACTION_CELL_CLICK = "com.moveoftoday.walkorwait.SUDOKU_CELL"
        private const val ACTION_NUMBER_CLICK = "com.moveoftoday.walkorwait.SUDOKU_NUM"

        // 4x4 스도쿠 퍼즐 (initial: 초기 숫자, 0은 빈칸)
        private val PUZZLES = listOf(
            Puzzle(
                initial = arrayOf(
                    intArrayOf(1, 0, 3, 0),
                    intArrayOf(0, 4, 0, 2),
                    intArrayOf(2, 0, 4, 0),
                    intArrayOf(0, 3, 0, 1)
                ),
                solution = arrayOf(
                    intArrayOf(1, 2, 3, 4),
                    intArrayOf(3, 4, 1, 2),
                    intArrayOf(2, 1, 4, 3),
                    intArrayOf(4, 3, 2, 1)
                )
            ),
            Puzzle(
                initial = arrayOf(
                    intArrayOf(0, 2, 0, 4),
                    intArrayOf(4, 0, 2, 0),
                    intArrayOf(0, 4, 0, 2),
                    intArrayOf(2, 0, 4, 0)
                ),
                solution = arrayOf(
                    intArrayOf(3, 2, 1, 4),
                    intArrayOf(4, 1, 2, 3),
                    intArrayOf(1, 4, 3, 2),
                    intArrayOf(2, 3, 4, 1)
                )
            ),
            Puzzle(
                initial = arrayOf(
                    intArrayOf(0, 1, 0, 3),
                    intArrayOf(3, 0, 1, 0),
                    intArrayOf(0, 3, 0, 1),
                    intArrayOf(1, 0, 3, 0)
                ),
                solution = arrayOf(
                    intArrayOf(4, 1, 2, 3),
                    intArrayOf(3, 2, 1, 4),
                    intArrayOf(2, 3, 4, 1),
                    intArrayOf(1, 4, 3, 2)
                )
            ),
            Puzzle(
                initial = arrayOf(
                    intArrayOf(2, 0, 0, 1),
                    intArrayOf(0, 1, 2, 0),
                    intArrayOf(0, 2, 1, 0),
                    intArrayOf(1, 0, 0, 2)
                ),
                solution = arrayOf(
                    intArrayOf(2, 4, 3, 1),
                    intArrayOf(3, 1, 2, 4),
                    intArrayOf(4, 2, 1, 3),
                    intArrayOf(1, 3, 4, 2)
                )
            ),
            Puzzle(
                initial = arrayOf(
                    intArrayOf(0, 0, 2, 1),
                    intArrayOf(2, 1, 0, 0),
                    intArrayOf(0, 0, 1, 2),
                    intArrayOf(1, 2, 0, 0)
                ),
                solution = arrayOf(
                    intArrayOf(4, 3, 2, 1),
                    intArrayOf(2, 1, 4, 3),
                    intArrayOf(3, 4, 1, 2),
                    intArrayOf(1, 2, 3, 4)
                )
            )
        )

        // 셀 ID 매핑
        private val CELL_IDS = arrayOf(
            intArrayOf(R.id.cell_00, R.id.cell_01, R.id.cell_02, R.id.cell_03),
            intArrayOf(R.id.cell_10, R.id.cell_11, R.id.cell_12, R.id.cell_13),
            intArrayOf(R.id.cell_20, R.id.cell_21, R.id.cell_22, R.id.cell_23),
            intArrayOf(R.id.cell_30, R.id.cell_31, R.id.cell_32, R.id.cell_33)
        )

        private val NUMBER_IDS = intArrayOf(R.id.num_1, R.id.num_2, R.id.num_3, R.id.num_4)

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = PreferenceManager(context)
            val puzzleIndex = prefs.getSudokuPuzzleIndex() % PUZZLES.size
            val puzzle = PUZZLES[puzzleIndex]
            val selected = prefs.getSudokuSelectedCell(appWidgetId)

            val views = RemoteViews(context.packageName, R.layout.widget_sudoku)

            // 스도쿠 진행률 계산 (채워진 칸 수 기준)
            val filledCells = countFilledCells(puzzle, prefs, appWidgetId)
            val totalEmptyCells = countEmptyCells(puzzle)
            val sudokuPercent = if (totalEmptyCells > 0) (filledCells * 100) / totalEmptyCells else 0

            // 스도쿠 진행률에 따른 펫 상태 결정
            val (animationType, emotionSymbol) = getSudokuStateByProgress(sudokuPercent)

            // 펫 아이콘 로드 (진행률 기반)
            val petBitmap = loadPetIconWithAnimation(context, prefs, animationType)
            if (petBitmap != null) {
                views.setImageViewBitmap(R.id.widget_pet_icon, petBitmap)
            } else {
                views.setImageViewResource(R.id.widget_pet_icon, R.mipmap.ic_launcher)
            }

            // 감정 심볼 표시
            views.setTextViewText(R.id.widget_emotion, emotionSymbol)

            // 완료 체크
            val isCompleted = checkPuzzleCompleted(puzzle, prefs, appWidgetId)
            val hasErrors = if (!isCompleted) checkHasErrors(puzzle, prefs, appWidgetId) else false

            // 상태 메시지 - Clear! 검정색
            val statusText = when {
                isCompleted -> "Clear!"
                hasErrors -> ""
                else -> ""
            }
            views.setTextViewText(R.id.widget_status, statusText)
            views.setTextColor(R.id.widget_status, 0xFF222222.toInt())

            // 완료시 펫 축하 반응
            if (isCompleted) {
                val celebrateBitmap = loadPetIconWithAnimation(context, prefs, "bark")
                if (celebrateBitmap != null) {
                    views.setImageViewBitmap(R.id.widget_pet_icon, celebrateBitmap)
                }
                views.setTextViewText(R.id.widget_emotion, "***")
            }

            // 셀 업데이트
            for (row in 0..3) {
                for (col in 0..3) {
                    val cellId = CELL_IDS[row][col]
                    val initialValue = puzzle.initial[row][col]
                    val userValue = prefs.getSudokuCellValue(appWidgetId, row, col)

                    val displayValue = if (initialValue != 0) {
                        initialValue.toString()
                    } else if (userValue != 0) {
                        userValue.toString()
                    } else {
                        ""
                    }

                    views.setTextViewText(cellId, displayValue)

                    // 초기값은 검정, 사용자 입력은 회색
                    val textColor = when {
                        initialValue != 0 -> 0xFF222222.toInt()
                        userValue != 0 -> 0xFF666666.toInt()
                        else -> 0xFF222222.toInt()
                    }
                    views.setTextColor(cellId, textColor)

                    // 셀 클릭 인텐트
                    if (initialValue == 0) {
                        val cellIntent = Intent(context, SudokuWidgetProvider::class.java).apply {
                            action = ACTION_CELL_CLICK
                            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            putExtra("row", row)
                            putExtra("col", col)
                        }
                        val cellPendingIntent = PendingIntent.getBroadcast(
                            context,
                            appWidgetId * 100 + row * 10 + col,
                            cellIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        views.setOnClickPendingIntent(cellId, cellPendingIntent)
                    }
                }
            }

            // 숫자 버튼 인텐트
            for (num in 1..4) {
                val numIntent = Intent(context, SudokuWidgetProvider::class.java).apply {
                    action = ACTION_NUMBER_CLICK
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra("number", num)
                }
                val numPendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId * 1000 + num,
                    numIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(NUMBER_IDS[num - 1], numPendingIntent)
            }

            // 새 퍼즐 버튼
            val newPuzzleIntent = Intent(context, SudokuWidgetProvider::class.java).apply {
                action = ACTION_NEW_PUZZLE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val newPuzzlePendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId + 10000,
                newPuzzleIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_new_puzzle, newPuzzlePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, SudokuWidgetProvider::class.java)
            )
            for (appWidgetId in appWidgetIds) {
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }

        /**
         * 퍼즐 완료 체크 - 정답과 일치하면 Clear!
         */
        private fun checkPuzzleCompleted(puzzle: Puzzle, prefs: PreferenceManager, appWidgetId: Int): Boolean {
            for (row in 0..3) {
                for (col in 0..3) {
                    val initialValue = puzzle.initial[row][col]
                    if (initialValue == 0) {
                        val userValue = prefs.getSudokuCellValue(appWidgetId, row, col)
                        // 빈칸이거나 정답과 다르면 미완료
                        if (userValue == 0 || userValue != puzzle.solution[row][col]) {
                            return false
                        }
                    }
                }
            }
            return true  // 모든 입력이 정답과 일치하면 Clear!
        }

        /**
         * 오류 있는지 체크
         */
        private fun checkHasErrors(puzzle: Puzzle, prefs: PreferenceManager, appWidgetId: Int): Boolean {
            for (row in 0..3) {
                for (col in 0..3) {
                    val initialValue = puzzle.initial[row][col]
                    if (initialValue == 0) {
                        val userValue = prefs.getSudokuCellValue(appWidgetId, row, col)
                        if (userValue != 0 && userValue != puzzle.solution[row][col]) {
                            return true
                        }
                    }
                }
            }
            return false
        }

        /**
         * 빈 칸 수 세기
         */
        private fun countEmptyCells(puzzle: Puzzle): Int {
            var count = 0
            for (row in 0..3) {
                for (col in 0..3) {
                    if (puzzle.initial[row][col] == 0) count++
                }
            }
            return count
        }

        /**
         * 채워진 칸 수 세기 (사용자 입력)
         */
        private fun countFilledCells(puzzle: Puzzle, prefs: PreferenceManager, appWidgetId: Int): Int {
            var count = 0
            for (row in 0..3) {
                for (col in 0..3) {
                    if (puzzle.initial[row][col] == 0) {
                        val userValue = prefs.getSudokuCellValue(appWidgetId, row, col)
                        if (userValue != 0) count++
                    }
                }
            }
            return count
        }

        /**
         * 스도쿠 진행률에 따른 펫 상태 결정 (ASCII만 사용)
         */
        private fun getSudokuStateByProgress(percent: Int): Pair<String, String> {
            return when (percent) {
                0 -> "idle" to "zzZ"
                in 1..25 -> "walk" to "?"
                in 26..50 -> "walk" to "~"
                in 51..75 -> "run" to "!!"
                in 76..99 -> "run" to "^o^"
                else -> "bark" to "***"  // 100%
            }
        }

        /**
         * 펫 아이콘 로드 (진행률 기반 애니메이션)
         */
        private fun loadPetIconWithAnimation(context: Context, prefs: PreferenceManager, animationType: String): Bitmap? {
            return try {
                val petTypeV2 = prefs.getPetTypeV2()
                if (petTypeV2 != null) {
                    val petLevel = prefs.getPetLevelV2()
                    val stage = petLevel.stage
                    val assetPath = "pets/${petTypeV2.folderName}/${stage.folderName}/$animationType/frame_000.png"
                    context.assets.open(assetPath).use { inputStream ->
                        BitmapFactory.decodeStream(inputStream)
                    }
                } else {
                    // V1 펫
                    val petTypeName = prefs.getPetType()
                    val petType = petTypeName?.let {
                        try { PetType.valueOf(it) } catch (e: Exception) { null }
                    }
                    if (petType != null) {
                        context.assets.open(petType.idleAssetPath).use { inputStream ->
                            val spriteSheet = BitmapFactory.decodeStream(inputStream)
                            if (spriteSheet != null) {
                                val frameWidth = spriteSheet.width / petType.idleFrames
                                Bitmap.createBitmap(spriteSheet, 0, 0, frameWidth, spriteSheet.height)
                            } else null
                        }
                    } else null
                }
            } catch (e: Exception) {
                // 애니메이션 폴더가 없으면 idle로 폴백
                try {
                    val petTypeV2 = prefs.getPetTypeV2()
                    if (petTypeV2 != null) {
                        val petLevel = prefs.getPetLevelV2()
                        val stage = petLevel.stage
                        val fallbackPath = "pets/${petTypeV2.folderName}/${stage.folderName}/idle/frame_000.png"
                        context.assets.open(fallbackPath).use { inputStream ->
                            BitmapFactory.decodeStream(inputStream)
                        }
                    } else null
                } catch (e2: Exception) {
                    null
                }
            }
        }

        /**
         * 펫 아이콘 로드 (grayscale) - 레거시
         */
        private fun loadPetIcon(context: Context, prefs: PreferenceManager): Bitmap? {
            return loadPetIconWithAnimation(context, prefs, "idle")?.let { toGrayscale(it) }
        }

        /**
         * Grayscale 변환
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
    }

    data class Puzzle(
        val initial: Array<IntArray>,
        val solution: Array<IntArray>
    )
}
