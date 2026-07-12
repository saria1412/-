package com.baynana.couplesgame

enum class Boldness(val rank: Int, val label: String, val emoji: String) {
    GENTLE(1, "ودّي", "🌷"),
    BOLD(2, "جريء", "🔥"),
    BOLDER(3, "أجرأ", "✨")
}

enum class ChallengeType(val label: String, val emoji: String) {
    SOLO("تحدٍّ فردي", "🎯"),
    TOGETHER("معًا", "🤝"),
    DUEL("مواجهة", "⚡"),
    SECRET("بطاقة سرية", "💌")
}

data class Challenge(
    val id: Int,
    val title: String,
    val prompt: String,
    val type: ChallengeType,
    val boldness: Boldness,
    val hearts: Int,
    val seconds: Int = 0,
    val category: String = "مرح"
)

data class GameResult(
    val challenge: Challenge,
    val completed: Boolean,
    val skippedSafely: Boolean = false,
    val winner: Int? = null,
    val doubled: Boolean = false
)
