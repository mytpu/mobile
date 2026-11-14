package com.example.mytpumobile

enum class WidgetSize(val label: String) {
    S("S"), M("M"), L("L")
}

enum class DashboardWidgetType(
    val title: String,
    val subtitle: String,
    val availableSizes: List<WidgetSize>
) {
    SCHEDULE("Сейчас и дальше", "rasp.tpu.ru", listOf(WidgetSize.S, WidgetSize.M, WidgetSize.L)),
    DEADLINES("Дедлайны", "lms.tpu.ru", listOf(WidgetSize.S, WidgetSize.M, WidgetSize.L)),
    GRADES("Оценки", "успеваемость", listOf(WidgetSize.S, WidgetSize.M, WidgetSize.L)),
    TICKETS("Мои заявки", "help.tpu.ru", listOf(WidgetSize.S, WidgetSize.M, WidgetSize.L)),
    MAIL_MOODLE("Почта и Moodle", "mail, lms", listOf(WidgetSize.S, WidgetSize.M, WidgetSize.L)),
    INSTRUCTIONS("Инструкции", "devdocs.tpu.ru · поиск", listOf(WidgetSize.M, WidgetSize.L)),
    MY_COURSES("Мои курсы", "Moodle · lms.tpu.ru", listOf(WidgetSize.M, WidgetSize.L))
}

data class DashboardWidgetConfig(
    val type: DashboardWidgetType,
    val size: WidgetSize,
    val isEnabled: Boolean = true
)

// Заглушки данных для Главного экрана
object MockDashboardData {
    const val dateHeader = "Четверг, 30 октября"
    const val groupBadge = "8К31"
    const val greeting = "Доброе утро, Анна"

    // Расписание
    const val currentTimeSlot = "10:25–12:00"
    const val currentSubject = "Математический анализ"
    const val currentDetails = "Лекция · корпус 10, ауд. 202 · Иванов А. П."
    const val remainingTime = "Осталось 34 мин"
    const val nextClass = "Дальше: Физика, 12:40"

    // Дедлайны
    const val deadlinesCount = 2
    const val nearestDeadlineTitle = "Лаба №3 Физика"
    const val nearestDeadlineWhen = "завтра"

    // Оценки
    const val averageGrade = "4,8"
    val recentGrades = listOf(5, 5, 4, 5)
    const val lastGradeNote = "Физика · 5, вчера"

    // Заявки
    const val ticketTitle = "Не входит в Moodle"
    const val ticketStatus = "нужен ответ"
    const val ticketMeta = "№102851 · исполнитель ответил 15:31"

    // Почта и Moodle
    val mailItems = listOf(
        Triple("Петров И. С.", "Лабораторную можно отработать в пятницу...", "9:12"),
        Triple("Moodle · Физика", "Открыто задание «Лабораторная №4»", "8:40")
    )
}