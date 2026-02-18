package com.moveoftoday.walkorwait

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.res.stringResource
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

    // Localized category strings
    val allCategory = stringResource(R.string.all)
    val categoryReading = stringResource(R.string.category_reading)
    val categoryMeditation = stringResource(R.string.category_meditation)
    val categoryStudy = stringResource(R.string.category_study)
    val categoryExercise = stringResource(R.string.category_exercise)
    val categoryWellness = stringResource(R.string.category_wellness)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember(allCategory) { mutableStateOf<String?>(allCategory) }

    val categories = listOf(allCategory, categoryReading, categoryMeditation, categoryStudy, categoryExercise, categoryWellness)

    val filteredChallenges = remember(searchQuery, selectedCategory, allCategory) {
        if (searchQuery.isNotBlank()) {
            challengeManager.searchChallenges(searchQuery)
        } else if (selectedCategory == allCategory) {
            challengeManager.getChallengesByCategory(null)
        } else {
            challengeManager.getChallengesByCategory(selectedCategory)
        }
    }

    // 전체 선택 시 카테고리별로 그룹화 (로컬라이즈된 카테고리명 사용)
    val challengesByCategory = remember(filteredChallenges, selectedCategory, allCategory) {
        if (selectedCategory == allCategory) {
            filteredChallenges.groupBy { it.type.getLocalizedCategory() }
        } else {
            emptyMap()
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
                text = stringResource(R.string.complete_challenge_earn_title),
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
                text = stringResource(R.string.challenge_list),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }

        // 챌린지 목록 (전체 선택 시 카테고리별 그룹화)
        if (selectedCategory == allCategory && challengesByCategory.isNotEmpty()) {
            // 카테고리별 섹션
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                challengesByCategory.forEach { (category, challenges) ->
                    // 카테고리 헤더
                    item {
                        Text(
                            text = category,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }

                    // 챌린지 그리드 (3열)
                    val rows = challenges.chunked(3)
                    items(rows.size) { rowIndex ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            rows[rowIndex].forEach { challenge ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ChallengeBox(
                                        challenge = challenge,
                                        completionCount = completionCounts[challenge.type] ?: 0,
                                        onClick = { onChallengeSelected(challenge) }
                                    )
                                }
                            }
                            // 빈 공간 채우기
                            repeat(3 - rows[rowIndex].size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else {
            // 특정 카테고리 선택 시 그리드
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 80.dp),
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
                color = Color.Black
            )
        }

        // 중앙: 타이틀
        Text(
            text = "rebon challenge",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
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
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
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
                color = Color.Black
            ),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(R.string.search_challenge),
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
                        color = Color.Black,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .background(
                        color = if (isSelected) Color.Black else Color.White,
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) Color.White else Color.Black
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
            .border(2.dp, Color.Black, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        // 아이콘 (중앙 상단)
        Image(
            painter = painterResource(id = challenge.iconRes),
            contentDescription = challenge.type.getLocalizedDisplayName(),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-16).dp)
                .height(72.dp),
            contentScale = ContentScale.FillHeight
        )

        // 이름 (하단 고정 - 완료 횟수 유무와 관계없이 동일 위치)
        Text(
            text = challenge.type.getLocalizedDisplayName(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-14).dp)
        )

        // 완료 횟수 (하단 고정)
        if (completionCount > 0) {
            Text(
                text = stringResource(R.string.completed_times, completionCount),
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
