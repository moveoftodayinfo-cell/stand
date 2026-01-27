package com.moveoftoday.walkorwait

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.*
import kotlin.math.sqrt

class StepSensorManager(private val context: Context) : SensorEventListener {
    private val TAG = "StepSensorManager"
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // Health Connect Manager
    private val healthConnectManager = HealthConnectManager(context)

    // 센서 우선순위: HEALTH_CONNECT > STEP_COUNTER > STEP_DETECTOR > ACCELEROMETER
    private var stepSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var stepDetectorSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    private val prefs = PreferenceManager(context)

    private var sensorType = SensorType.NONE

    // Health Connect용
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var healthConnectJob: Job? = null

    // TYPE_STEP_COUNTER용
    private var initialSteps = -1

    // TYPE_STEP_DETECTOR, ACCELEROMETER용
    private var currentSteps = 0

    // 가속도계용 변수
    private var previousY = 0f
    private var currentY = 0f
    private var previousStepDetected = 0L
    private val stepThreshold = 11.0f // 걸음 감지 임계값
    private val stepInterval = 400L // 최소 걸음 간격 (밀리초)

    var onStepCountChanged: ((Int) -> Unit)? = null
    var onDistanceChanged: ((Double) -> Unit)? = null  // km 단위

    enum class SensorType {
        HEALTH_CONNECT,  // 최우선
        STEP_COUNTER,
        STEP_DETECTOR,
        ACCELEROMETER,
        NONE
    }

    init {
        // init에서는 동기적으로 가능한 체크만 수행
        // 실제 권한 체크와 데이터 로드는 startListening()에서
        Log.d(TAG, "StepSensorManager initialized")
    }

    private fun initializeDataSource() {
        // 데이터 소스 우선순위에 따라 선택
        // 1. 사용자가 Health Connect 연결을 설정했는지 확인
        val useHealthConnect = prefs.useHealthConnect()
        val isHCAvailable = healthConnectManager.isAvailable()

        Log.d(TAG, "🔍 initializeDataSource - useHealthConnect: $useHealthConnect, isAvailable: $isHCAvailable")

        if (useHealthConnect && isHCAvailable) {
            // Health Connect를 사용하도록 설정된 경우에만 사용
            sensorType = SensorType.HEALTH_CONNECT
            Log.d(TAG, "🏃 Health Connect enabled by user, will use Health Connect")
        }
        // 2. Health Connect 미사용 또는 사용 불가 시 기본 센서 사용
        else if (stepSensor != null) {
            sensorType = SensorType.STEP_COUNTER
            Log.d(TAG, "✅ Using STEP_COUNTER sensor")
            if (!useHealthConnect) {
                Toast.makeText(context, "기본 걸음 센서 사용", Toast.LENGTH_SHORT).show()
            }
        }
        // 3. STEP_DETECTOR 센서
        else if (sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR).also {
            stepDetectorSensor = it
        } != null) {
            sensorType = SensorType.STEP_DETECTOR
            currentSteps = prefs.getTodaySteps()
            Log.d(TAG, "✅ Using STEP_DETECTOR sensor")
            Toast.makeText(context, "걸음 감지 센서 사용", Toast.LENGTH_SHORT).show()
        }
        // 4. ACCELEROMETER (최후 수단)
        else if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).also {
            accelerometerSensor = it
        } != null) {
            sensorType = SensorType.ACCELEROMETER
            currentSteps = prefs.getTodaySteps()
            Log.d(TAG, "⚙️ Using ACCELEROMETER sensor")
            Toast.makeText(context, "가속도계로 걸음 감지 (센서 없음)", Toast.LENGTH_LONG).show()
        }
        // 5. 사용 가능한 것이 없음
        else {
            sensorType = SensorType.NONE
            Log.e(TAG, "❌ No sensors available")
            Toast.makeText(context, "사용 가능한 센서 없음", Toast.LENGTH_LONG).show()
        }
    }

    private fun fallbackToSensor() {
        Log.d(TAG, "⚠️ Falling back to sensor")
        when {
            stepSensor != null -> {
                sensorType = SensorType.STEP_COUNTER
                Log.d(TAG, "✅ Fallback to STEP_COUNTER")
                Toast.makeText(context, "걸음 센서 사용", Toast.LENGTH_SHORT).show()
            }
            sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR).also {
                stepDetectorSensor = it
            } != null -> {
                sensorType = SensorType.STEP_DETECTOR
                currentSteps = prefs.getTodaySteps()
                Log.d(TAG, "✅ Fallback to STEP_DETECTOR")
                Toast.makeText(context, "걸음 감지 센서 사용", Toast.LENGTH_SHORT).show()
            }
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).also {
                accelerometerSensor = it
            } != null -> {
                sensorType = SensorType.ACCELEROMETER
                currentSteps = prefs.getTodaySteps()
                Log.d(TAG, "⚙️ Fallback to ACCELEROMETER")
                Toast.makeText(context, "가속도계로 걸음 감지", Toast.LENGTH_SHORT).show()
            }
            else -> {
                sensorType = SensorType.NONE
                Log.e(TAG, "❌ No fallback available")
            }
        }
    }

    fun startListening() {
        // 데이터 소스 초기화
        initializeDataSource()

        Log.d(TAG, "=== startListening called, Type: $sensorType ===")

        when (sensorType) {
            SensorType.HEALTH_CONNECT -> {
                // ⚠️ 기본 센서 리스너 완전히 해제 (중복 측정 방지)
                sensorManager.unregisterListener(this)
                Log.d(TAG, "🔌 Basic sensor listener unregistered for Health Connect mode")

                // Health Connect 권한 체크 및 데이터 가져오기
                scope.launch {
                    try {
                        val hasPermissions = healthConnectManager.hasAllPermissions()
                        if (hasPermissions) {
                            val connectedAppName = prefs.getConnectedFitnessAppName()
                            Log.d(TAG, "🏃 Using HEALTH_CONNECT - $connectedAppName")
                            val toastMsg = if (connectedAppName.isNotEmpty())
                                "✅ $connectedAppName 연결됨 (기본 센서 OFF)"
                            else
                                "✅ 피트니스 앱 연결됨 (기본 센서 OFF)"
                            Toast.makeText(context, toastMsg, Toast.LENGTH_SHORT).show()

                            // 즉시 첫 데이터 로드
                            val initialSteps = healthConnectManager.getTodaySteps()
                            val initialDistance = healthConnectManager.getTodayDistance() / 1000.0 // 미터 -> km

                            currentSteps = initialSteps
                            prefs.saveTodaySteps(initialSteps)
                            prefs.saveTodayDistance(initialDistance)

                            Log.d(TAG, "📊 Health Connect initial - steps: $initialSteps, distance: ${initialDistance}km")
                            onStepCountChanged?.invoke(initialSteps)
                            onDistanceChanged?.invoke(initialDistance)

                            // 주기적으로 업데이트 (5초마다)
                            healthConnectJob = scope.launch {
                                while (isActive) {
                                    delay(5000) // 5초 대기
                                    try {
                                        val steps = healthConnectManager.getTodaySteps()
                                        val distance = healthConnectManager.getTodayDistance() / 1000.0 // 미터 -> km

                                        currentSteps = steps
                                        prefs.saveTodaySteps(steps)
                                        prefs.saveTodayDistance(distance)

                                        Log.d(TAG, "📊 Health Connect updated - steps: $steps, distance: ${distance}km")
                                        onStepCountChanged?.invoke(steps)
                                        onDistanceChanged?.invoke(distance)
                                    } catch (e: Exception) {
                                        Log.e(TAG, "❌ Failed to update Health Connect data: ${e.message}")
                                    }
                                }
                            }
                            Log.d(TAG, "HEALTH_CONNECT polling started")
                        } else {
                            // 권한 없으면 사용자에게 알림 후 센서로 fallback
                            Log.d(TAG, "⚠️ Health Connect permissions not granted")
                            Toast.makeText(
                                context,
                                "⚠️ 피트니스 앱 연결이 끊어졌습니다.\n설정에서 재연결하세요",
                                Toast.LENGTH_LONG
                            ).show()
                            fallbackToSensor()
                            startListeningSensor() // 센서 리스닝 시작
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Health Connect error: ${e.message}")
                        Toast.makeText(
                            context,
                            "⚠️ 피트니스 앱 연결 오류.\n기본 센서로 전환합니다",
                            Toast.LENGTH_LONG
                        ).show()
                        fallbackToSensor()
                        startListeningSensor() // 센서 리스닝 시작
                    }
                }
            }
            else -> {
                startListeningSensor()
            }
        }
    }

    private fun startListeningSensor() {
        when (sensorType) {
            SensorType.STEP_COUNTER -> {
                stepSensor?.let {
                    val savedInitialSteps = prefs.getInitialSteps()
                    if (savedInitialSteps > 0) {
                        initialSteps = savedInitialSteps
                        Log.d(TAG, "Loaded initial steps: $savedInitialSteps")
                    }
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
                    Log.d(TAG, "STEP_COUNTER listener registered")
                }
            }
            SensorType.STEP_DETECTOR -> {
                stepDetectorSensor?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
                    Log.d(TAG, "STEP_DETECTOR listener registered")
                }
            }
            SensorType.ACCELEROMETER -> {
                accelerometerSensor?.let {
                    sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
                    Log.d(TAG, "ACCELEROMETER listener registered")
                }
            }
            SensorType.NONE -> {
                Log.e(TAG, "No sensor available to start")
            }
            SensorType.HEALTH_CONNECT -> {
                // 이미 Health Connect로 처리됨
            }
        }
    }

    fun stopListening() {
        Log.d(TAG, "stopListening called")

        // Health Connect Job 취소
        healthConnectJob?.cancel()
        healthConnectJob = null

        // 센서 리스너 해제
        sensorManager.unregisterListener(this)

        // HEALTH_CONNECT, STEP_DETECTOR, ACCELEROMETER 사용 시 현재 걸음 수 저장
        if (sensorType == SensorType.HEALTH_CONNECT ||
            sensorType == SensorType.STEP_DETECTOR ||
            sensorType == SensorType.ACCELEROMETER) {
            prefs.saveTodaySteps(currentSteps)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (sensorType) {
                SensorType.HEALTH_CONNECT -> {
                    // Health Connect는 센서 이벤트 사용 안 함 (주기적 polling)
                }
                SensorType.STEP_COUNTER -> handleStepCounter(it)
                SensorType.STEP_DETECTOR -> handleStepDetector(it)
                SensorType.ACCELEROMETER -> handleAccelerometer(it)
                SensorType.NONE -> {}
            }
        }
    }

    private fun handleStepCounter(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSteps = event.values[0].toInt()

            if (initialSteps == -1) {
                initialSteps = totalSteps
                prefs.saveInitialSteps(totalSteps)
                Log.d(TAG, "Initial steps saved: $initialSteps")
            }

            currentSteps = totalSteps - initialSteps
            val estimatedDistance = currentSteps / 1250.0 // 1km = 약 1,250걸음

            Log.d(TAG, "Steps: $currentSteps (Total: $totalSteps), Distance: ${estimatedDistance}km")
            prefs.saveTodayDistance(estimatedDistance)

            onStepCountChanged?.invoke(currentSteps)
            onDistanceChanged?.invoke(estimatedDistance)
        }
    }

    private fun handleStepDetector(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
            currentSteps++
            val estimatedDistance = currentSteps / 1250.0 // 1km = 약 1,250걸음

            Log.d(TAG, "Step detected! Total: $currentSteps, Distance: ${estimatedDistance}km")
            prefs.saveTodayDistance(estimatedDistance)

            onStepCountChanged?.invoke(currentSteps)
            onDistanceChanged?.invoke(estimatedDistance)
        }
    }

    private fun handleAccelerometer(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // 가속도 크기 계산
            val acceleration = sqrt(x * x + y * y + z * z)

            previousY = currentY
            currentY = acceleration

            // 걸음 감지: 가속도 변화가 임계값을 넘고, 충분한 시간이 지났을 때
            val currentTime = System.currentTimeMillis()
            if (currentY > stepThreshold &&
                previousY <= stepThreshold &&
                currentTime - previousStepDetected > stepInterval) {

                previousStepDetected = currentTime
                currentSteps++
                val estimatedDistance = currentSteps / 1250.0 // 1km = 약 1,250걸음

                Log.d(TAG, "Step detected via accelerometer! Total: $currentSteps, Distance: ${estimatedDistance}km")
                prefs.saveTodayDistance(estimatedDistance)

                onStepCountChanged?.invoke(currentSteps)
                onDistanceChanged?.invoke(estimatedDistance)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        Log.d(TAG, "Accuracy changed: $accuracy")
    }

    fun isSensorAvailable(): Boolean {
        val available = sensorType != SensorType.NONE
        Log.d(TAG, "isSensorAvailable: $available")
        return available
    }

    fun resetDailySteps() {
        Log.d(TAG, "resetDailySteps called")

        when (sensorType) {
            SensorType.HEALTH_CONNECT -> {
                // Health Connect는 외부 앱의 데이터를 사용하므로 리셋 불가
                // 대신 로컬 저장값만 초기화
                currentSteps = 0
                prefs.saveTodaySteps(0)
                Log.d(TAG, "Health Connect local data reset")
            }
            SensorType.STEP_COUNTER -> {
                initialSteps = -1
                prefs.saveInitialSteps(-1)
            }
            SensorType.STEP_DETECTOR, SensorType.ACCELEROMETER -> {
                currentSteps = 0
                prefs.saveTodaySteps(0)
            }
            SensorType.NONE -> {}
        }

        Toast.makeText(context, "걸음 수 리셋!", Toast.LENGTH_SHORT).show()
    }
}