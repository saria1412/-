package com.baynana.couplesgame

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.max

private val Burgundy = Color(0xFF6C183E)
private val DeepWine = Color(0xFF2B0B1C)
private val Rose = Color(0xFFE693AA)
private val Gold = Color(0xFFF2C76E)
private val Cream = Color(0xFFFFF8F3)
private val FriendlyGreen = Color(0xFF57A57D)

private val DarkScheme: ColorScheme = darkColorScheme(
    primary = Rose,
    onPrimary = Color(0xFF3A071F),
    primaryContainer = Burgundy,
    onPrimaryContainer = Color.White,
    secondary = Gold,
    onSecondary = Color(0xFF3A2800),
    background = Color(0xFF160B11),
    onBackground = Color(0xFFFFF4F7),
    surface = Color(0xFF24131B),
    onSurface = Color(0xFFFFF4F7),
    surfaceVariant = Color(0xFF39232D),
    onSurfaceVariant = Color(0xFFE9D7DE),
    outline = Color(0xFF9B7D89)
)

private val LightScheme: ColorScheme = lightColorScheme(
    primary = Burgundy,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE7EE),
    onPrimaryContainer = Color(0xFF3E0A25),
    secondary = Color(0xFFA57417),
    onSecondary = Color.White,
    background = Cream,
    onBackground = Color(0xFF2A1A21),
    surface = Color.White,
    onSurface = Color(0xFF2A1A21),
    surfaceVariant = Color(0xFFFFEDF2),
    onSurfaceVariant = Color(0xFF5C4650),
    outline = Color(0xFF9D828D)
)

@Composable
fun BaynanaTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (androidx.compose.foundation.isSystemInDarkTheme()) DarkScheme else LightScheme,
        content = content
    )
}

private enum class AppScreen { SETUP, PLAY, RESULTS }

@Composable
fun BaynanaGame() {
    var screen by rememberSaveable { mutableStateOf(AppScreen.SETUP) }
    var nameOne by rememberSaveable { mutableStateOf("الزوج") }
    var nameTwo by rememberSaveable { mutableStateOf("الزوجة") }
    var boldnessRank by rememberSaveable { mutableIntStateOf(2) }
    var challengeCount by rememberSaveable { mutableIntStateOf(15) }
    var mutualConsent by rememberSaveable { mutableStateOf(false) }
    var showSetupError by rememberSaveable { mutableStateOf(false) }

    var round by remember { mutableStateOf<List<Challenge>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var scoreOne by remember { mutableIntStateOf(0) }
    var scoreTwo by remember { mutableIntStateOf(0) }
    var coupleScore by remember { mutableIntStateOf(0) }
    var doubleOneAvailable by remember { mutableStateOf(true) }
    var doubleTwoAvailable by remember { mutableStateOf(true) }
    val results = remember { mutableStateListOf<GameResult>() }

    fun startRound() {
        val level = Boldness.entries.first { it.rank == boldnessRank }
        if (level == Boldness.BOLDER && !mutualConsent) {
            showSetupError = true
            return
        }
        nameOne = nameOne.trim().ifBlank { "الزوج" }
        nameTwo = nameTwo.trim().ifBlank { "الزوجة" }
        round = ChallengeBank.createRound(level, challengeCount)
        currentIndex = 0
        scoreOne = 0
        scoreTwo = 0
        coupleScore = 0
        doubleOneAvailable = true
        doubleTwoAvailable = true
        results.clear()
        showSetupError = false
        screen = AppScreen.PLAY
    }

    when (screen) {
        AppScreen.SETUP -> SetupScreen(
            nameOne, { nameOne = it }, nameTwo, { nameTwo = it },
            boldnessRank, {
                boldnessRank = it
                if (it < 3) mutualConsent = false
                showSetupError = false
            },
            challengeCount, { challengeCount = it },
            mutualConsent, {
                mutualConsent = it
                showSetupError = false
            },
            showSetupError, ::startRound
        )

        AppScreen.PLAY -> PlayScreen(
            nameOne, nameTwo, round, currentIndex, scoreOne, scoreTwo, coupleScore,
            doubleOneAvailable, doubleTwoAvailable,
            onComplete = { winner, doubled ->
                val challenge = round[currentIndex]
                val assignee = currentIndex % 2
                val multiplier = if (doubled) 2 else 1
                val gained = challenge.hearts * multiplier
                when (challenge.type) {
                    ChallengeType.SOLO, ChallengeType.SECRET -> {
                        if (assignee == 0) {
                            scoreOne += gained
                            if (doubled) doubleOneAvailable = false
                        } else {
                            scoreTwo += gained
                            if (doubled) doubleTwoAvailable = false
                        }
                    }
                    ChallengeType.TOGETHER -> {
                        coupleScore += gained
                        scoreOne += gained / 2
                        scoreTwo += gained / 2
                    }
                    ChallengeType.DUEL -> when (winner) {
                        0 -> scoreOne += gained
                        1 -> scoreTwo += gained
                        else -> {
                            scoreOne += gained / 2
                            scoreTwo += gained / 2
                        }
                    }
                }
                results += GameResult(challenge, true, winner = winner, doubled = doubled)
                if (currentIndex >= round.lastIndex) screen = AppScreen.RESULTS else currentIndex += 1
            },
            onSafeSkip = {
                results += GameResult(round[currentIndex], false, skippedSafely = true)
                if (currentIndex >= round.lastIndex) screen = AppScreen.RESULTS else currentIndex += 1
            },
            onExit = { screen = AppScreen.SETUP }
        )

        AppScreen.RESULTS -> ResultsScreen(
            nameOne, nameTwo, scoreOne, scoreTwo, coupleScore, results,
            onReplay = ::startRound,
            onSetup = { screen = AppScreen.SETUP }
        )
    }
}

@Composable
private fun SetupScreen(
    nameOne: String,
    onNameOneChange: (String) -> Unit,
    nameTwo: String,
    onNameTwoChange: (String) -> Unit,
    boldnessRank: Int,
    onBoldnessChange: (Int) -> Unit,
    challengeCount: Int,
    onChallengeCountChange: (Int) -> Unit,
    mutualConsent: Boolean,
    onConsentChange: (Boolean) -> Unit,
    showError: Boolean,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f), MaterialTheme.colorScheme.background)))
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))
        AppMark()
        Spacer(Modifier.height(14.dp))
        Text("بيننا", fontSize = 38.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Text("تحديات، ضحك، وقرب أكثر", fontSize = 17.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(22.dp))
        FriendlyPanel(
            "أجرأ... لكن ألطف",
            "بطاقات زوجية مرحة وحميمية. الموافقة أولًا، والابتسامة دائمًا، والتبديل متاح بلا لوم أو خصم.",
            "💞"
        )
        Spacer(Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text("من يلعب؟", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(nameOne, onNameOneChange, Modifier.fillMaxWidth(), label = { Text("اسم الطرف الأول") }, singleLine = true, shape = RoundedCornerShape(16.dp))
                OutlinedTextField(nameTwo, onNameTwoChange, Modifier.fillMaxWidth(), label = { Text("اسم الطرف الثاني") }, singleLine = true, shape = RoundedCornerShape(16.dp))
                HorizontalDivider(Modifier.padding(vertical = 3.dp))
                Text("درجة الجرأة", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Boldness.entries.forEach { level ->
                        FilterChip(
                            selected = boldnessRank == level.rank,
                            onClick = { onBoldnessChange(level.rank) },
                            label = { Text("${level.emoji} ${level.label}") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Text(
                    when (boldnessRank) {
                        1 -> "مرح، ذكريات، وضحك خفيف."
                        2 -> "مغازلة، قرب، وتحديات رومانسية."
                        else -> "حميمية راقية وطلبات سرية بموافقة الطرفين."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                if (boldnessRank == 3) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(16.dp)) {
                        Row(
                            Modifier.fillMaxWidth().clickable { onConsentChange(!mutualConsent) }.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(mutualConsent, onConsentChange)
                            Spacer(Modifier.width(8.dp))
                            Text("نحن موافقان على بطاقات الجرأة العالية، ويمكن لأي طرف التبديل فورًا.", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Text("عدد البطاقات", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(10, 15, 20).forEach { count ->
                        FilterChip(challengeCount == count, { onChallengeCountChange(count) }, { Text("$count") }, modifier = Modifier.weight(1f))
                    }
                }
                if (showError) Text("فعّل موافقة الطرفين أولًا، أو اختر مستوى جريء.", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                Button(onStart, Modifier.fillMaxWidth().height(58.dp), shape = RoundedCornerShape(18.dp)) {
                    Text("ابدآ ليلة التحديات 🔥", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AppMark() {
    Box(
        Modifier.size(94.dp).background(Brush.radialGradient(listOf(Rose, Burgundy, DeepWine)), CircleShape).border(2.dp, Gold.copy(alpha = 0.9f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text("💬", fontSize = 44.sp, modifier = Modifier.padding(end = 18.dp, bottom = 10.dp))
        Text("❤", fontSize = 42.sp, color = Gold, modifier = Modifier.padding(start = 22.dp, top = 20.dp))
        Text("✦", fontSize = 20.sp, color = Color.White, modifier = Modifier.align(Alignment.TopEnd).padding(13.dp))
    }
}

@Composable
private fun FriendlyPanel(title: String, text: String, emoji: String) {
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f), shape = RoundedCornerShape(22.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Text(emoji, fontSize = 30.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.height(4.dp))
                Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 21.sp)
            }
        }
    }
}

@Composable
private fun PlayScreen(
    nameOne: String,
    nameTwo: String,
    round: List<Challenge>,
    currentIndex: Int,
    scoreOne: Int,
    scoreTwo: Int,
    coupleScore: Int,
    doubleOneAvailable: Boolean,
    doubleTwoAvailable: Boolean,
    onComplete: (Int?, Boolean) -> Unit,
    onSafeSkip: () -> Unit,
    onExit: () -> Unit
) {
    if (round.isEmpty()) return
    val challenge = round[currentIndex]
    val assignee = currentIndex % 2
    val assigneeName = if (assignee == 0) nameOne else nameTwo
    val canDouble = when (challenge.type) {
        ChallengeType.SOLO, ChallengeType.SECRET -> if (assignee == 0) doubleOneAvailable else doubleTwoAvailable
        else -> false
    }
    var revealed by remember(currentIndex) { mutableStateOf(challenge.type != ChallengeType.SECRET) }
    var doubled by remember(currentIndex) { mutableStateOf(false) }
    var timerRunning by remember(currentIndex) { mutableStateOf(false) }
    var timerRemaining by remember(currentIndex) { mutableIntStateOf(challenge.seconds) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(timerRunning, currentIndex) {
        while (timerRunning && timerRemaining > 0) {
            delay(1000)
            timerRemaining -= 1
        }
        if (timerRemaining == 0) timerRunning = false
    }

    val glow by animateFloatAsState(if (revealed) 1f else 0.94f, spring(), label = "cardGlow")

    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)))).statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onExit, contentPadding = PaddingValues(horizontal = 12.dp)) { Text("خروج") }
            Spacer(Modifier.weight(1f))
            Text("${currentIndex + 1} / ${round.size}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(8.dp))
        ScoreBar(nameOne, nameTwo, scoreOne, scoreTwo, coupleScore)
        Spacer(Modifier.height(12.dp))
        AnimatedContent(
            targetState = currentIndex,
            transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.92f)) togetherWith (fadeOut() + scaleOut(targetScale = 0.92f)) using SizeTransform(clip = false) },
            label = "challengeCard"
        ) {
            Card(
                Modifier.fillMaxWidth().weight(1f).scale(glow),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                if (!revealed) SecretCard(assigneeName) {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    revealed = true
                } else ChallengeContent(challenge, assigneeName, timerRemaining, timerRunning) {
                    if (challenge.seconds > 0) {
                        if (timerRemaining == 0) timerRemaining = challenge.seconds
                        timerRunning = !timerRunning
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (revealed) {
            if (canDouble) {
                Surface(Modifier.fillMaxWidth().clickable { doubled = !doubled }, color = if (doubled) Gold.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Switch(doubled, { doubled = it })
                        Spacer(Modifier.width(10.dp))
                        Text(if (doubled) "بطاقة ×2 مفعّلة — الشجاعة مضاعفة 💛" else "استخدم بطاقة ×2 في هذا التحدي؟", fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(Modifier.height(9.dp))
            }
            if (challenge.type == ChallengeType.DUEL) {
                Text("من فاز بالمواجهة؟", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button({ onComplete(0, false) }, Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text(nameOne, maxLines = 1) }
                    Button({ onComplete(1, false) }, Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text(nameTwo, maxLines = 1) }
                    FilledTonalButton({ onComplete(null, false) }, Modifier.weight(0.8f), shape = RoundedCornerShape(16.dp)) { Text("تعادل") }
                }
            } else {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onComplete(null, doubled)
                    },
                    modifier = Modifier.fillMaxWidth().height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Burgundy),
                    shape = RoundedCornerShape(18.dp)
                ) { Text("تمّ التحدي — أضف القلوب ❤️", fontSize = 17.sp, fontWeight = FontWeight.Bold) }
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onSafeSkip, Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) { Text("غير مريح؟ بدّل البطاقة بلطف 🌿") }
        }
    }
}

@Composable
private fun ScoreBar(nameOne: String, nameTwo: String, scoreOne: Int, scoreTwo: Int, coupleScore: Int) {
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), shape = RoundedCornerShape(20.dp), tonalElevation = 3.dp) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            ScorePill(nameOne, scoreOne)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💞", fontSize = 23.sp)
                Text("$coupleScore", fontWeight = FontWeight.Bold, color = Gold)
            }
            ScorePill(nameTwo, scoreTwo)
        }
    }
}

@Composable
private fun ScorePill(name: String, score: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(105.dp)) {
        Text(name, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text("$score ❤️", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun SecretCard(assigneeName: String, onReveal: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("💌", fontSize = 78.sp)
            Spacer(Modifier.height(15.dp))
            Text("بطاقة سرية لـ $assigneeName", fontSize = 25.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("قرّب الشاشة منك، ثم اكشف البطاقة. يمكنك تبديلها فورًا إن لم تناسبك.", textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 22.sp)
            Spacer(Modifier.height(24.dp))
            Button(onReveal, shape = RoundedCornerShape(18.dp)) { Text("اكشف التحدي ✨", fontSize = 17.sp) }
        }
    }
}

@Composable
private fun ChallengeContent(challenge: Challenge, assigneeName: String, timerRemaining: Int, timerRunning: Boolean, onTimerToggle: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            AssistChip({}, { Text("${challenge.type.emoji} ${challenge.type.label}") })
            AssistChip({}, { Text("${challenge.boldness.emoji} ${challenge.boldness.label}") })
        }
        Spacer(Modifier.height(12.dp))
        Text(
            when (challenge.type) {
                ChallengeType.SOLO, ChallengeType.SECRET -> "الدور على $assigneeName"
                ChallengeType.TOGETHER -> "هذا التحدي لكما معًا"
                ChallengeType.DUEL -> "مواجهة بينكما"
            },
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(14.dp))
        Text(challenge.title, fontSize = 30.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Surface(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f), shape = RoundedCornerShape(22.dp)) {
            Text(challenge.prompt, Modifier.padding(20.dp), fontSize = 19.sp, lineHeight = 29.sp, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.weight(1f))
        Text("${challenge.hearts} قلبًا ❤️", fontSize = 21.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        if (challenge.seconds > 0) {
            Spacer(Modifier.height(13.dp))
            FilledTonalButton(onTimerToggle, shape = RoundedCornerShape(16.dp)) {
                Text(
                    when {
                        timerRemaining == 0 -> "أعد المؤقت ⏱"
                        timerRunning -> "متبقٍ $timerRemaining ثانية — إيقاف"
                        else -> "ابدأ مؤقت $timerRemaining ثانية"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text("الموافقة والراحة أهم من النقاط.", fontSize = 13.sp, color = FriendlyGreen, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ResultsScreen(
    nameOne: String,
    nameTwo: String,
    scoreOne: Int,
    scoreTwo: Int,
    coupleScore: Int,
    results: List<GameResult>,
    onReplay: () -> Unit,
    onSetup: () -> Unit
) {
    val context = LocalContext.current
    val completed = results.count { it.completed }
    val skipped = results.count { it.skippedSafely }
    val total = scoreOne + scoreTwo + coupleScore
    val winnerText = when {
        scoreOne > scoreTwo -> "$nameOne بطل الجرأة الليلة"
        scoreTwo > scoreOne -> "$nameTwo بطل الجرأة الليلة"
        else -> "تعادل جميل بينكما"
    }
    val reward = remember(total) {
        listOf(
            "الفائز يختار الحلوى أو المشروب.",
            "اختارا أغنية وارقصا دقيقة إضافية.",
            "10 دقائق بلا هواتف مع حديث هادئ.",
            "كل طرف يمنح الآخر طلبًا لطيفًا واحدًا.",
            "خططا لتفصيل مفاجئ في موعدكما القادم."
        )[max(0, total) % 5]
    }
    LazyColumn(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)))).statusBarsPadding().navigationBarsPadding(),
        contentPadding = PaddingValues(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("🏆", fontSize = 68.sp)
            Text("انتهت ليلة التحديات", fontSize = 30.sp, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text(winnerText, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(26.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        ResultScore(nameOne, scoreOne)
                        ResultScore("معًا", coupleScore)
                        ResultScore(nameTwo, scoreTwo)
                    }
                    HorizontalDivider(Modifier.padding(vertical = 14.dp))
                    Text("نفذتما $completed تحديات، وبدّلتما $skipped بطاقات بأمان.", textAlign = TextAlign.Center)
                }
            }
        }
        item { FriendlyPanel("مكافأة الليلة", reward, "🎁") }
        item {
            val shareText = "لعبنا بيننا: $nameOne $scoreOne قلبًا، $nameTwo $scoreTwo قلبًا، ومجموعنا المشترك $coupleScore 💞"
            OutlinedButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "مشاركة النتيجة"))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) { Text("شارك النتيجة 💌") }
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onReplay, Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("جولة جديدة") }
                FilledTonalButton(onSetup, Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text("تغيير الإعدادات") }
            }
        }
        item {
            Text("أجمل جولة هي التي تنتهي بابتسامة وراحة للطرفين.", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ResultScore(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(95.dp)) {
        Text(label, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text("$value", fontSize = 28.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
        Text("قلوب")
    }
}
