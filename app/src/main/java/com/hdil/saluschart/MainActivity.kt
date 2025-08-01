package com.hdil.saluschart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hdil.saluschart.core.chart.ChartPoint
import com.hdil.saluschart.core.chart.RangeChartPoint
import com.hdil.saluschart.ui.compose.charts.LineChart
import com.hdil.saluschart.ui.compose.charts.ScatterPlot
import com.hdil.saluschart.ui.theme.SalusChartTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.ui.graphics.Color
import com.hdil.saluschart.ui.compose.charts.BarChart
import com.hdil.saluschart.ui.compose.charts.CalendarChart
import com.hdil.saluschart.ui.compose.charts.CalendarEntry
import com.hdil.saluschart.ui.compose.charts.PieChart
import com.hdil.saluschart.ui.compose.charts.RangeBarChart
import com.hdil.saluschart.ui.compose.charts.StackedBarChart
import com.hdil.saluschart.core.chart.StackedChartPoint
import java.time.LocalDate
import java.time.YearMonth


import kotlin.text.toInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SalusChartTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SampleCharts(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Composable
fun SampleCharts(modifier: Modifier = Modifier) {
    // 차트 타입 선택 상태 관리
    var selectedChartType by remember { mutableStateOf("Line") }

//    // 현재 연월과 랜덤 데이터 생성
    val yearMonth = YearMonth.now()
//    val random = java.util.Random(0)
//    val entries = (1..28).map { day ->
//        val date = yearMonth.atDay(day)
//        val value = random.nextFloat() * 100
//        CalendarEntry(
//            date = date,
//            value = value,
//            color = if (random.nextBoolean()) null else Color.Green
//        )
//    }
    val startDate = LocalDate.of(yearMonth.year, 6, 1)
    val endDate = LocalDate.of(yearMonth.year, 7, 15)
    val random = java.util.Random(0)
    val entries = generateSequence(startDate) { date ->
        if (date.isBefore(endDate)) date.plusDays(1) else null
    }.map { date ->
        val value = random.nextFloat() * 100
        CalendarEntry(
            date = date,
            value = value,
            color = if (random.nextBoolean()) null else Color.Green
        )
    }.toList()

    // 기본적인 raw 데이터로 차트 그리기
    val sampleData = listOf(10f, 25f, 40f, 20f, 35f, 55f, 45f)
    val weekDays = listOf("월", "화", "수", "목", "금", "토", "일")

    // 드롭다운 메뉴를 위한 상태 관리
    var widthExpanded by remember { mutableStateOf(false) }
    var heightExpanded by remember { mutableStateOf(false) }

    val widthOptions = listOf(150.dp, 200.dp, 250.dp, 300.dp, 350.dp)
    val heightOptions = listOf(150.dp, 200.dp, 250.dp, 300.dp, 350.dp)

    var selectedWidth by remember { mutableStateOf(widthOptions[1]) }
    var selectedHeight by remember { mutableStateOf(heightOptions[1]) }

    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 차트 타입 선택 토글 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            SingleChoiceSegmentedButtonRow {
                SegmentedButton(
                    selected = selectedChartType == "Line",
                    onClick = { selectedChartType = "Line" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 7),
                    label = { Text("Line", fontSize = 8.sp) }
                )
                SegmentedButton(
                    selected = selectedChartType == "Scatter",
                    onClick = { selectedChartType = "Scatter" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 7),
                    label = { Text("Scatter", fontSize = 7.sp) }
                )
                SegmentedButton(
                    selected = selectedChartType == "Bar",
                    onClick = { selectedChartType = "Bar" },
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 7),
                    label = { Text("Bar", fontSize = 8.sp) }
                )
                SegmentedButton(
                    selected = selectedChartType == "Stacked",
                    onClick = { selectedChartType = "Stacked" },
                    shape = SegmentedButtonDefaults.itemShape(index = 3, count = 7),
                    label = { Text("Stacked", fontSize = 7.sp) }
                )
                SegmentedButton(
                    selected = selectedChartType == "Range",
                    onClick = { selectedChartType = "Range" },
                    shape = SegmentedButtonDefaults.itemShape(index = 4, count = 7),
                    label = { Text("Range", fontSize = 8.sp) }
                )
                SegmentedButton(
                    selected = selectedChartType == "Pie",
                    onClick = { selectedChartType = "Pie" },
                    shape = SegmentedButtonDefaults.itemShape(index = 5, count = 7),
                    label = { Text("Pie", fontSize = 8.sp) }
                )
                SegmentedButton(
                    selected = selectedChartType == "Calendar",
                    onClick = { selectedChartType = "Calendar" },
                    shape = SegmentedButtonDefaults.itemShape(index = 6, count = 7),
                    label = { Text("Calendar", fontSize = 7.sp) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Width 드롭다운
            Column {
                Box {
                    OutlinedButton(
                        onClick = { widthExpanded = !widthExpanded },
                    ) {
                        Text("차트 너비: ${selectedWidth.value.toInt()}dp")
                    }

                    DropdownMenu(
                        expanded = widthExpanded,
                        onDismissRequest = { widthExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        widthOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text("${option.value.toInt()}dp") },
                                onClick = {
                                    selectedWidth = option
                                    widthExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Height 드롭다운
            Column {
                Box {
                    OutlinedButton(
                        onClick = { heightExpanded = !heightExpanded },
                    ) {
                        Text("차트 높이: ${selectedHeight.value.toInt()}dp")
                    }

                    DropdownMenu(
                        expanded = heightExpanded,
                        onDismissRequest = { heightExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        heightOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text("${option.value.toInt()}dp") },
                                onClick = {
                                    selectedHeight = option
                                    heightExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // ChartPoint 리스트로 변환
        val chartPoints = sampleData.mapIndexed { index, value ->
            ChartPoint(
                x = index.toFloat(),
                y = value,
                label = weekDays.getOrElse(index) { "" }
            )
        }

        // 범위 차트용 샘플 데이터 (심박수 범위 예시)
        val rangeData = listOf(
            RangeChartPoint(x = 0f, yMin = 54f, yMax = 160f, label = "2일"),
            RangeChartPoint(x = 1f, yMin = 65f, yMax = 145f, label = "3일"),
            RangeChartPoint(x = 2f, yMin = 58f, yMax = 125f, label = "4일"),
            RangeChartPoint(x = 3f, yMin = 75f, yMax = 110f, label = "6일"),
            RangeChartPoint(x = 4f, yMin = 68f, yMax = 162f, label = "7일"),
            RangeChartPoint(x = 5f, yMin = 72f, yMax = 168f, label = "8일"),
            RangeChartPoint(x = 6f, yMin = 65f, yMax = 138f, label = "9일"),
            RangeChartPoint(x = 7f, yMin = 85f, yMax = 105f, label = "10일")
        )

        // 스택 바 차트용 샘플 데이터 (일별 영양소 섭취량 예시)
        val stackedData = listOf(
            StackedChartPoint(
                x = 0f,
                values = listOf(80f, 45f, 120f), // 단백질, 지방, 탄수화물 (g)
                label = "월"
            ),
            StackedChartPoint(
                x = 1f,
                values = listOf(75f, 38f, 110f),
                label = "화"
            ),
            StackedChartPoint(
                x = 2f,
                values = listOf(90f, 52f, 140f),
                label = "수"
            ),
            StackedChartPoint(
                x = 3f,
                values = listOf(85f, 41f, 135f),
                label = "목"
            ),
            StackedChartPoint(
                x = 4f,
                values = listOf(95f, 58f, 150f),
                label = "금"
            ),
            StackedChartPoint(
                x = 5f,
                values = listOf(70f, 35f, 100f),
                label = "토"
            ),
            StackedChartPoint(
                x = 6f,
                values = listOf(88f, 48f, 125f),
                label = "일"
            )
        )

        // 스택 바 차트용 세그먼트 레이블 (한 번만 정의)
        val segmentLabels = listOf("단백질", "지방", "탄수화물")

        // 선택된 차트 타입에 따라 다른 차트 표시
        when (selectedChartType) {
            "Line" -> {
                LineChart(
                    data = chartPoints,
                    title = "요일별 활동량",
                    yLabel = "활동량",
                    xLabel = "요일",
                    width = selectedWidth,
                    height = selectedHeight
                )
            }
            "Scatter" -> {
                ScatterPlot(
                    data = chartPoints,
                    title = "요일별 활동량",
                    yLabel = "활동량",
                    xLabel = "요일",
                    width = selectedWidth,
                    height = selectedHeight
                )
            }
            "Bar" -> {
                BarChart(
                    data = chartPoints,
                    title = "요일별 활동량",
                    yLabel = "활동량",
                    xLabel = "요일",
                    width = selectedWidth,
                    height = selectedHeight
                )
            }
            "Stacked" -> {
                StackedBarChart(
                    data = stackedData,
                    segmentLabels = segmentLabels,
                    title = "요일별 영양소 섭취량",
                    yLabel = "영양소 (g)",
                    xLabel = "요일",
                    width = selectedWidth,
                    height = selectedHeight,
                    showLegend = true,
                    colors = listOf(
                        Color(0xFF2196F3), // 파랑 (단백질)
                        Color(0xFFFF9800), // 주황 (지방) 
                        Color(0xFF4CAF50)  // 초록 (탄수화물)
                    )
                )
            }
            "Range" -> {
                RangeBarChart(
                    data = rangeData,
                    title = "일별 심박수 범위",
                    yLabel = "심박수 (bpm)",
                    xLabel = "날짜",
                    width = selectedWidth,
                    height = selectedHeight,
                    barColor = Color(0xFFFF9800)
                )
            }
            "Pie" -> {
                PieChart(
                    data = chartPoints,
                    title = "요일별 활동량",
                    isDonut = true,
                    showLegend = true,
                    width = selectedWidth,
                    height = selectedHeight
                )
            }
            "Calendar" -> {
                CalendarChart(
                    entries = entries,
                    yearMonth = yearMonth,
                    width = selectedWidth,
                    height = selectedHeight
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChartPreview() {
    SalusChartTheme {
        SampleCharts()
    }
}