package com.moveoftoday.walkorwait.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore
import com.moveoftoday.walkorwait.HapticManager
import com.moveoftoday.walkorwait.pet.MockupColors
import com.moveoftoday.walkorwait.pet.rememberKenneyFont
import java.text.NumberFormat
import java.util.Locale

/**
 * Firestore analytics/revenue 문서 데이터 모델
 */
private data class RevenueData(
    val arpu: Long = 0,
    val arppu: Long = 0,
    val conversionRate: Double = 0.0,
    val ltv: Long = 0,
    val monthlyPrice: Long = 0,
    val yearlyPrice: Long = 0,
    val updatedAt: String = ""
)

/**
 * 관리자 수익 분석 대시보드
 */
@Composable
fun SettingsAdminDashboard(
    hapticManager: HapticManager,
    onBack: () -> Unit
) {
    val kenneyFont = rememberKenneyFont()

    var revenueData by remember { mutableStateOf<RevenueData?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Firestore에서 데이터 로드
    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance()
            .document("analytics/revenue")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    revenueData = RevenueData(
                        arpu = doc.getLong("arpu") ?: 0,
                        arppu = doc.getLong("arppu") ?: 0,
                        conversionRate = doc.getDouble("conversionRate") ?: 0.0,
                        ltv = doc.getLong("ltv") ?: 0,
                        monthlyPrice = doc.getLong("monthlyPrice") ?: 0,
                        yearlyPrice = doc.getLong("yearlyPrice") ?: 0,
                        updatedAt = doc.getString("updatedAt") ?: ""
                    )
                } else {
                    errorMessage = "No data"
                }
                isLoading = false
            }
            .addOnFailureListener { e ->
                errorMessage = e.message ?: "Load failed"
                isLoading = false
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MockupColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsHeader(
                title = "admin",
                kenneyFont = kenneyFont,
                onBack = {
                    hapticManager.click()
                    onBack()
                }
            )

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MockupColors.Border)
                    }
                }

                errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MockupColors.TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }

                revenueData != null -> {
                    AdminDashboardContent(
                        data = revenueData!!,
                        kenneyFont = kenneyFont
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardContent(
    data: RevenueData,
    kenneyFont: androidx.compose.ui.text.font.FontFamily
) {
    val scrollState = rememberScrollState()
    val currencyFormat = remember {
        NumberFormat.getCurrencyInstance(Locale.KOREA).apply {
            maximumFractionDigits = 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 수익 분석 섹션
        SectionTitle(text = "Revenue", kenneyFont = kenneyFont)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                label = "ARPU",
                value = currencyFormat.format(data.arpu),
                kenneyFont = kenneyFont,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "ARPPU",
                value = currencyFormat.format(data.arppu),
                kenneyFont = kenneyFont,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                label = "CVR",
                value = "${data.conversionRate}%",
                kenneyFont = kenneyFont,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "LTV",
                value = currencyFormat.format(data.ltv),
                kenneyFont = kenneyFont,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 구독 정보 섹션
        SectionTitle(text = "Subscription", kenneyFont = kenneyFont)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard(
                label = "Monthly",
                value = currencyFormat.format(data.monthlyPrice),
                kenneyFont = kenneyFont,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                label = "Yearly",
                value = currencyFormat.format(data.yearlyPrice),
                kenneyFont = kenneyFont,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 마지막 업데이트
        if (data.updatedAt.isNotEmpty()) {
            Text(
                text = "Last updated: ${data.updatedAt}",
                fontSize = 12.sp,
                color = MockupColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    kenneyFont: androidx.compose.ui.text.font.FontFamily
) {
    Text(
        text = text,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MockupColors.TextPrimary,
        fontFamily = kenneyFont
    )
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    kenneyFont: androidx.compose.ui.text.font.FontFamily,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .border(2.dp, MockupColors.Border, RoundedCornerShape(10.dp))
            .background(MockupColors.CardBackground, RoundedCornerShape(10.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MockupColors.TextPrimary,
            fontFamily = kenneyFont
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MockupColors.TextMuted
        )
    }
}
