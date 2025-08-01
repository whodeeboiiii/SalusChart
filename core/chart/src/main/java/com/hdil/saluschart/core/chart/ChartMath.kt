package com.hdil.saluschart.core.chart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import java.time.YearMonth
import java.time.LocalDate
import kotlin.div
import kotlin.text.toDouble
import kotlin.text.toFloat
import kotlin.times
import kotlin.math.*

object ChartMath {
    /**
     * 차트 그리기에 필요한 메트릭 정보를 담는 데이터 클래스
     *
     * @param paddingX X축 패딩 값
     * @param paddingY Y축 패딩 값
     * @param chartWidth 차트의 실제 너비
     * @param chartHeight 차트의 실제 높이
     * @param minY Y축의 최소값
     * @param maxY Y축의 최대값
     * @param yTicks Y축에 표시할 눈금 값들
     */
    data class ChartMetrics(
        val paddingX: Float, 
        val paddingY: Float, 
        val chartWidth: Float, 
        val chartHeight: Float, 
        val minY: Float, 
        val maxY: Float,
        val yTicks: List<Float>
    )

    /**
     * y-axis 눈금 값들을 계산합니다. 
     * 1, 2, 5의 배수를 사용하여 시각적으로 깔끔한 눈금을 생성합니다.
     *
     * @param min 데이터의 최소값
     * @param max 데이터의 최대값
     * @param tickCount 원하는 눈금 개수 (기본값: 5)
     * @return 계산된 눈금 값들의 리스트
     */
    fun computeNiceTicks(min: Float, max: Float, tickCount: Int = 5): List<Float> {
        if (min >= max) {
            return listOf(0f, 1f)
        }
        val rawStep = (max - min) / tickCount.toDouble()
        val power = 10.0.pow(floor(log10(rawStep)))
        val candidates = listOf(1.0, 2.0, 5.0).map { it * power }
        val step = candidates.minByOrNull { abs(it - rawStep) } ?: power

        val niceMin = floor(min / step) * step
        val niceMax = ceil(max / step) * step
        
        val ticks = mutableListOf<Float>()
        var t = niceMin
        while (t <= niceMax + 1e-6) {
            // Fix floating-point precision issues
            val roundedTick = round(t * 1000000) / 1000000
            ticks.add(roundedTick.toFloat())
            t += step
        }
        
        return ticks
    }

    /**
     * 차트 그리기에 필요한 메트릭 값을 계산합니다.
     *
     * @param size Canvas의 전체 크기
     * @param values 차트에 표시할 Y축 데이터 값 목록
     * @param tickCount 원하는 Y축 눈금 개수 (기본값: 5)
     * @param chartType 차트 타입 (BAR/STACKED_BAR 타입일 경우 minY를 항상 0으로 설정)
     * @return 차트 메트릭 객체
     */
    fun computeMetrics(size: Size, values: List<Float>, tickCount: Int = 5, chartType: ChartType? = null): ChartMetrics { // TODO: tickCount = 5 고정 
        val paddingX = 60f
        val paddingY = 40f
        val chartWidth = size.width - paddingX
        val chartHeight = size.height - paddingY
        
        val dataMax = values.maxOrNull() ?: 1f
        val dataMin = values.minOrNull() ?: 0f
        
        // BAR 및 STACKED_BAR 차트의 경우 항상 minY를 0으로 설정
        val minY = if (chartType == ChartType.BAR || chartType == ChartType.STACKED_BAR) {
            0f
        } else {
            if (dataMin >= 0 && dataMin < dataMax * 0.1) 0f else dataMin
        }
        val maxY = dataMax
        
        val yTicks = computeNiceTicks(minY, maxY, tickCount)
        
        val actualMinY = yTicks.minOrNull() ?: minY
        val actualMaxY = yTicks.maxOrNull() ?: maxY
        
        return ChartMetrics(paddingX, paddingY, chartWidth, chartHeight, actualMinY, actualMaxY, yTicks)
    }

    /**
     * 데이터 포인트를 화면 좌표로 변환합니다.
     *
     * @param data 차트 데이터 포인트 목록
     * @param size Canvas의 전체 크기
     * @param metrics 차트 메트릭 정보
     * @return 화면 좌표로 변환된 Offset 목록
     */
    fun mapToCanvasPoints(data: List<ChartPoint>, size: Size, metrics: ChartMetrics): List<Offset> {
        val spacing = metrics.chartWidth / (data.size - 1)
        return data.mapIndexed { i, point ->
            val x = metrics.paddingX + i * spacing
            val y = metrics.chartHeight - ((point.y - metrics.minY) / (metrics.maxY - metrics.minY)) * metrics.chartHeight
            Offset(x, y)
        }
    }

    /**
     * 파이 차트의 중심점과 반지름을 계산합니다.
     *
     * @param size 캔버스 크기
     * @param padding 원 테두리 패딩 값
     * @return Pair(중심 좌표, 반지름)
     */
    fun computePieMetrics(size: Size, padding: Float = 32f): Pair<Offset, Float> {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = minOf(size.width, size.height) / 2 - padding
        return Pair(center, radius)
    }

    /**
     * 파이 차트의 각 섹션의 각도를 계산합니다.
     *
     * @param data 차트 데이터 포인트 목록
     * @return List<Triple<시작 각도, 스윕 각도, 값 비율>>
     */
    fun computePieAngles(data: List<ChartPoint>): List<Triple<Float, Float, Float>> {
        val totalValue = data.sumOf { it.y.toDouble() }.toFloat()
        if (totalValue <= 0f) return emptyList()

        var startAngle = -90f // 12시 방향에서 시작

        return data.map { point ->
            val ratio = point.y / totalValue
            val sweepAngle = ratio * 360f
            val result = Triple(startAngle, sweepAngle, ratio)
            startAngle += sweepAngle
            result
        }
    }

    /**
     * 파이 섹션의 레이블 위치를 계산합니다.
     *
     * @param center 원의 중심점
     * @param radius 원의 기본 반지름
     * @param radiusFactor 레이블 위치 조정을 위한 반지름 인수 (1보다 작으면 안쪽, 크면 바깥쪽)
     * @param angle 각도(라디안)
     * @return 레이블이 표시될 위치 좌표
     */
    fun calculateLabelPosition(center: Offset, radius: Float, radiusFactor: Float, angleInDegrees: Float): Offset {
        val angleInRadians = Math.toRadians(angleInDegrees.toDouble())
        val labelRadius = radius * radiusFactor
        val x = center.x + labelRadius * Math.cos(angleInRadians).toFloat()
        val y = center.y + labelRadius * Math.sin(angleInRadians).toFloat()
        return Offset(x, y)
    }

    /**
     * 캘린더 차트에서 사용될 데이터 클래스
     *
     * @param date 날짜
     * @param value 데이터 값
     * @param color 색상 코드 (null인 경우 기본 색상 사용)
     */
    data class CalendarData(val date: LocalDate, val value: Float, val color: Int? = null)

    /**
     * 캘린더에 필요한 정보를 계산합니다.
     *
     * @param yearMonth 표시할 년월
     * @return 달력 구성에 필요한 정보 (첫 번째 요일 위치, 해당 월의 일 수, 필요한 행 수)
     */
    fun computeCalendarMetrics(yearMonth: YearMonth): Triple<Int, Int, Int> {
        val firstDayOfMonth = yearMonth.atDay(1)
        val lastDayOfMonth = yearMonth.atEndOfMonth()
        val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 일요일이 0이 되도록 조정
        val totalDays = lastDayOfMonth.dayOfMonth

        // 필요한 행의 수 계산 (첫 요일 위치 + 일수에 따라 필요한 행 결정)
        val weeks = (firstDayOfWeek + totalDays + 6) / 7

        return Triple(firstDayOfWeek, totalDays, weeks)
    }

    /**
     * 값에 따른 원의 크기를 계산합니다.
     *
     * @param value 현재 값
     * @param maxValue 가능한 최대값
     * @param minSize 최소 원 크기
     * @param maxSize 최대 원 크기
     * @return 계산된 원의 크기
     */
    fun calculateBubbleSize(value: Float, maxValue: Float, minSize: Float, maxSize: Float): Float {
        if (maxValue <= 0f) return minSize
        val normalizedValue = value / maxValue
        return minSize + (maxSize - minSize) * normalizedValue
    }

    /**
     * 범위 차트 그리기에 필요한 메트릭 값을 계산합니다.
     *
     * @param size Canvas의 전체 크기
     * @param data 범위 차트 데이터 포인트 목록
     * @param tickCount 원하는 Y축 눈금 개수 (기본값: 5)
     * @return 차트 메트릭 객체
     */
    fun computeRangeMetrics(size: Size, data: List<RangeChartPoint>, tickCount: Int = 5): ChartMetrics {
        val paddingX = 60f
        val paddingY = 40f
        val chartWidth = size.width - paddingX
        val chartHeight = size.height - paddingY
        
        val allYValues = data.flatMap { listOf(it.yMin, it.yMax) }
        val dataMax = allYValues.maxOrNull() ?: 1f
        val dataMin = allYValues.minOrNull() ?: 0f
        
        val yTicks = computeNiceTicks(dataMin, dataMax, tickCount)
        
        val actualMinY = yTicks.minOrNull() ?: dataMin
        val actualMaxY = yTicks.maxOrNull() ?: dataMax
        
        return ChartMetrics(paddingX, paddingY, chartWidth, chartHeight, actualMinY, actualMaxY, yTicks)
    }
}