package com.example.mytpumobile

enum class TicketStatus(val label: String) {
    NEED_REPLY("нужен ответ"),
    IN_PROGRESS("в работе"),
    RESOLVED("решена")
}

data class TicketTimelineStep(
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean = false
)

data class TicketComment(
    val author: String,
    val time: String,
    val text: String,
    val isStudent: Boolean
)

data class TpuTicket(
    val id: String,
    val department: String, // "ИТ-служба" или "кампус"
    val title: String,
    val status: TicketStatus,
    val lastMessagePreview: String,
    val lastUpdateTime: String,
    val deadlineText: String,
    val timeline: List<TicketTimelineStep>,
    val comments: List<TicketComment>
)

data class TicketDraft(
    val serviceName: String,
    val description: String,
    val attachmentName: String? = null
)

object MockTicketsData {
    val initialTickets = listOf(
        TpuTicket(
            id = "102851",
            department = "ИТ-служба",
            title = "Не входит в Moodle после смены пароля",
            status = TicketStatus.NEED_REPLY,
            lastMessagePreview = "«Попробуйте войти через 15 минут»",
            lastUpdateTime = "15:31",
            deadlineText = "ответ до 30.10",
            timeline = listOf(
                TicketTimelineStep("Зарегистрирована", "29.10, 12:22", isCompleted = true),
                TicketTimelineStep("Назначена ИТ-службе", "29.10, 12:40", isCompleted = true),
                TicketTimelineStep("В работе", "29.10, 15:31 · исполнитель ответил", isCompleted = true, isCurrent = true),
                TicketTimelineStep("Решена", "", isCompleted = false),
                TicketTimelineStep("Закрыта", "после вашего подтверждения", isCompleted = false)
            ),
            comments = listOf(
                TicketComment(
                    author = "Вы · через помощника",
                    time = "12:22",
                    text = "После смены пароля 26.10 Moodle пишет «неверный пароль». По инструкции devdocs выходил везде, чистил cookie и входил через ТПУ — не помогло.",
                    isStudent = true
                ),
                TicketComment(
                    author = "ИТ-служба ТПУ",
                    time = "15:31",
                    text = "Сбросили сессии Moodle и синхронизацию пароля. Попробуйте войти через 15 минут и напишите, получилось ли.",
                    isStudent = false
                )
            )
        ),
        TpuTicket(
            id = "102723",
            department = "кампус",
            title = "Плита в общежитии №12",
            status = TicketStatus.IN_PROGRESS,
            lastMessagePreview = "«Электрик придёт 19.09 после 15:00»",
            lastUpdateTime = "09:40",
            deadlineText = "срок 2–5 дней",
            timeline = listOf(
                TicketTimelineStep("Зарегистрирована", "18.09, 09:12", isCompleted = true),
                TicketTimelineStep("Назначена службе кампуса", "18.09, 09:40", isCompleted = true),
                TicketTimelineStep("В работе", "Электрик придёт 19.09 после 15:00", isCompleted = true, isCurrent = true),
                TicketTimelineStep("Решена", "", isCompleted = false)
            ),
            comments = listOf(
                TicketComment(
                    author = "Вы",
                    time = "09:12",
                    text = "Не работает конфорка плиты на кухне 4 этажа, искрит при включении.",
                    isStudent = true
                ),
                TicketComment(
                    author = "Служба кампуса",
                    time = "09:40",
                    text = "Электрик придёт 19.09 после 15:00. Пожалуйста, не включайте конфорку.",
                    isStudent = false
                )
            )
        )
    )
}