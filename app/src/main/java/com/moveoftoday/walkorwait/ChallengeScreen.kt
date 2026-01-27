package com.moveoftoday.walkorwait

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moveoftoday.walkorwait.pet.PixelIcon
import com.moveoftoday.walkorwait.pet.rememberKenneyFont

@Composable
fun ChallengeScreen(
    onBack: () -> Unit,
    onChallengeSelected: (Challenge) -> Unit
) {
    val context = LocalContext.current
    val challengeManager = remember { ChallengeManager.getInstance(context) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>("전체") }

    val categories = listOf("전체", "독서", "명상", "공부")

    val filteredChallenges = remember(searchQuery, selectedCategory) {
        if (searchQuery.isNotBlank()) {
            challengeManager.searchChallenges(searchQuery)
        } else {
            challengeManager.getChallengesByCategory(selectedCategory)
        }
    }

    val completionCounts by challengeManager.todayCompletionCounts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp)
            .padding(top = 30.dp)
    ) {

        // 헤더
        ChallengeHeader(onBack = onBack)

        Spacer(modifier = Modifier.height(16.dp))

        // 검색창
        SearchBox(
            query = searchQuery,
            onQueryChange = { searchQuery = it }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 카테고리 탭
        CategoryTabs(
            categories = categories,
            selectedCategory = selectedCategory,
            onCategorySelected = { selectedCategory = it }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 칭호 획득 안내
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "챌린지를 완료하고 펫 칭호를 획득하세요!",
                fontSize = 13.sp,
                color = Color(0xFF666666),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 챌린지 목록 타이틀
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            PixelIcon(
                iconName = "icon_trophy",
                size = 20.dp,
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "챌린지 목록",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        }

        // 챌린지 그리드
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredChallenges) { challenge ->
                ChallengeBox(
                    challenge = challenge,
                    completionCount = completionCounts[challenge.type] ?: 0,
                    onClick = { onChallengeSelected(challenge) }
                )
            }
        }
    }
}

@Composable
private fun ChallengeHeader(onBack: () -> Unit) {
    val kenneyFont = rememberKenneyFont()

    Box(modifier = Modifier.fillMaxWidth()) {
        // 좌측: 뒤로가기 버튼
        Box(
            modifier = Modifier
                .clickable { onBack() }
                .align(Alignment.CenterStart)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "←",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF333333)
            )
        }

        // 중앙: 타이틀
        Text(
            text = "rebon challenge",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            fontFamily = kenneyFont,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun SearchBox(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
            .background(Color.White, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PixelIcon(
            iconName = "icon_magnifier",
            size = 20.dp,
            tint = Color.Black
        )

        Spacer(modifier = Modifier.width(12.dp))

        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            textStyle = TextStyle(
                fontSize = 14.sp,
                color = Color(0xFF333333)
            ),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = "챌린지 검색...",
                        fontSize = 14.sp,
                        color = Color(0xFF999999)
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun CategoryTabs(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            Box(
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = Color(0xFF333333),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .background(
                        color = if (isSelected) Color(0xFF333333) else Color.White,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.White else Color(0xFF333333)
                )
            }
        }
    }
}

@Composable
private fun ChallengeBox(
    challenge: Challenge,
    completionCount: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .border(2.dp, Color(0xFF333333), RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        // 아이콘 + 이름 (중앙 고정)
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Image(
                    painter = painterResource(id = challenge.iconRes),
                    contentDescription = challenge.name,
                    modifier = Modifier.height(36.dp),
                    contentScale = ContentScale.FillHeight
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = challenge.name,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333),
                textAlign = TextAlign.Center
            )
        }

        // 완료 횟수 (하단 고정)
        if (completionCount > 0) {
            Text(
                text = "${completionCount}회 완료",
                fontSize = 9.sp,
                color = Color(0xFF666666),
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = 4.dp)
            )
        }
    }
}
