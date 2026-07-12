#!/usr/bin/env python3
from pathlib import Path
import re
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else 'BaynanaAndroid')
source = root / 'app/src/main/java/com/baynana/couplesgame'
tests = root / 'app/src/test/java/com/baynana/couplesgame'

# Fix the launch crash: always install a real launcher Activity.
(source / 'MainActivity.kt').write_text('''package com.baynana.couplesgame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BaynanaTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    BaynanaGame()
                }
            }
        }
    }
}
''', encoding='utf-8')

models = source / 'Models.kt'
text = models.read_text(encoding='utf-8')
text = text.replace(
    '    INTIMATE(4, "حميمي", "🌙", "للزوجين فقط وبموافقة واضحة")',
    '    INTIMATE(4, "حميمي", "🌙", "قرب وطلبات خاصة بموافقة واضحة"),\n'
    '    PRIVATE(5, "خاص جدًا", "🖤", "بطاقات أعمق للزوجين فقط")'
)
text = text.replace(
    '    SPICY("ليلة جريئة", "🔥", "مزيج أقرب وأكثر حميمية للزوجين")',
    '    SPICY("ليلة خاصة", "🔥", "قرب أعمق وبطاقات سرية للزوجين")'
)
models.write_text(text, encoding='utf-8')

app = source / 'BaynanaApp.kt'
text = app.read_text(encoding='utf-8')
text = text.replace(
    'if (level == Boldness.INTIMATE && (!marriedAdults || !mutualConsent))',
    'if (level.rank >= Boldness.INTIMATE.rank && (!marriedAdults || !mutualConsent))'
)
text = text.replace(
    'if (boldnessRank == Boldness.INTIMATE.rank) {',
    'if (boldnessRank >= Boldness.INTIMATE.rank) {'
)
text = text.replace('فعّلا التأكيدين أولًا لفتح المستوى الحميمي.', 'فعّلا التأكيدين أولًا لفتح المستويات الخاصة.')
text = text.replace(
    'Text(if (level == Boldness.INTIMATE) "🔐" else "💌", fontSize = 76.sp)',
    'Text(if (level.rank >= Boldness.INTIMATE.rank) "🔐" else "💌", fontSize = 76.sp)'
)
text = text.replace(
    'color = if (challenge.boldness == Boldness.INTIMATE) Wine.copy(alpha = .14f)',
    'color = if (challenge.boldness.rank >= Boldness.INTIMATE.rank) Wine.copy(alpha = .14f)'
)
text = text.replace('🔐 فتح المستوى الحميمي', '🔐 فتح المستويات الخاصة')
text = text.replace('ليلتكما… بطريقتكما', 'ليلتكما الخاصة… بطريقتكما')
app.write_text(text, encoding='utf-8')

bank = source / 'ChallengeBank.kt'
text = bank.read_text(encoding='utf-8')
marker = '        Challenge(90, "النهاية من اختياركما", "اختارا معًا ختامًا مريحًا للجولة: موسيقى وقرب، حديث خاص، تدليل، أو تحدٍّ من صنعكما.", ChallengeType.TOGETHER, Boldness.INTIMATE, 40, 0, "ختام", setOf(NightMood.ROMANTIC, NightMood.SPICY))'
private_cards = r'''
        Challenge(91, "بداية لا يراها سواكما", "اختارا من: همسة قريبة، حضن أطول، قبلة بطيئة، أو طلب خاص قابل للتعديل. لا يبدأ التحدي إلا بعد موافقة الطرفين.", ChallengeType.CHOICE, Boldness.PRIVATE, 44, 0, "بداية", setOf(NightMood.SPICY, NightMood.ROMANTIC), true),
        Challenge(92, "ثلاث رغبات", "كل طرف يكتب ثلاث رغبات: واحدة الآن، واحدة لوقت لاحق، وواحدة يتمنى الحديث عنها فقط. اكشفا ما تختارانه معًا.", ChallengeType.SECRET, Boldness.PRIVATE, 46, 0, "رغبات", setOf(NightMood.SPICY, NightMood.DISCOVERY), true),
        Challenge(93, "عينان مغمضتان", "يغلق أحدكما عينيه، ويختار الآخر لمسة لطيفة متفقًا عليها لليد أو الوجه أو الشعر أو الكتف. بدّلا الأدوار بعد 30 ثانية.", ChallengeType.TOGETHER, Boldness.PRIVATE, 44, 60, "حواس", setOf(NightMood.SPICY, NightMood.ROMANTIC)),
        Challenge(94, "إشارة القبلة", "اتفقا على إشارة سرية تعني: أريد قبلة. جرّباها مرة الآن فقط إن كانت مريحة لكليكما.", ChallengeType.TOGETHER, Boldness.PRIVATE, 42, 0, "قبلة", setOf(NightMood.SPICY, NightMood.ROMANTIC)),
        Challenge(95, "القرب المثالي", "كل طرف يصف كيف تبدأ لحظة القرب المثالية بالنسبة إليه: المكان، الإضاءة، الكلام، والمسافة. الاستماع وحده يكفي.", ChallengeType.SECRET, Boldness.PRIVATE, 44, 0, "اكتشاف", setOf(NightMood.DISCOVERY, NightMood.SPICY), true),
        Challenge(96, "نعم • ربما • ليس الآن", "اختارا ثلاثة أفكار رومانسية خاصة، وصنّفا كل واحدة إلى: نعم، ربما لاحقًا، أو ليست مناسبة الآن. لا يوجد جواب خاطئ.", ChallengeType.CHOICE, Boldness.PRIVATE, 46, 0, "حدود", setOf(NightMood.DISCOVERY, NightMood.SPICY), true),
        Challenge(97, "خمس دقائق لنا", "اضبطا مؤقت خمس دقائق، أخفضا الإضاءة، وأبعدا الهاتف. اختارا قربًا أو حديثًا أو موسيقى دون أي هدف آخر.", ChallengeType.TOGETHER, Boldness.PRIVATE, 48, 300, "أجواء", setOf(NightMood.SPICY, NightMood.ROMANTIC)),
        Challenge(98, "همسة حرارة", "اقترب وهمس بأكثر عبارة إعجاب خاصة وراقية تستطيع قولها لشريكك هذه الليلة.", ChallengeType.SOLO, Boldness.PRIVATE, 42, 0, "مغازلة", setOf(NightMood.SPICY), true),
        Challenge(99, "اختيار المسافة", "شريكك يختار المسافة المريحة بينكما وطريقة الجلوس لمدة دقيقة؛ أنت تلتزم باختياره كاملًا.", ChallengeType.SOLO, Boldness.PRIVATE, 42, 60, "ثقة", setOf(NightMood.SPICY, NightMood.ROMANTIC)),
        Challenge(100, "دقيقة قيادة", "يتولى أحدكما قيادة دقيقة رومانسية كاملة بالكلام فقط: اقترب، توقف، أمسك يدي، انظر إليّ. كلمة «توقف» تنهيها فورًا.", ChallengeType.TOGETHER, Boldness.PRIVATE, 48, 60, "ثقة", setOf(NightMood.SPICY, NightMood.DISCOVERY), true),
        Challenge(101, "رسالة بلا كلمات", "عبّر لشريكك عن رغبتك في القرب باستخدام النظرة والإشارة فقط لمدة 45 ثانية، ثم أخبره بما قصدت.", ChallengeType.DUEL, Boldness.PRIVATE, 44, 45, "إشارات", setOf(NightMood.SPICY, NightMood.PLAYFUL)),
        Challenge(102, "سر العطر", "اقترب بالقدر المريح وقل ما الرائحة أو العطر الذي يرتبط لديك بأكثر لحظاتكما قربًا.", ChallengeType.SOLO, Boldness.PRIVATE, 40, 0, "حواس", setOf(NightMood.SPICY, NightMood.ROMANTIC)),
        Challenge(103, "الاقتراب البطيء", "ابدآ من مسافة مريحة واقتربا ببطء شديد. يتوقف التحدي عندما يقول أحدكما: هنا مناسب.", ChallengeType.TOGETHER, Boldness.PRIVATE, 46, 45, "قرب", setOf(NightMood.SPICY, NightMood.ROMANTIC)),
        Challenge(104, "أربع مفاجآت", "اكتب أربع مفاجآت قصيرة: همسة، حضن، قبلة مريحة، وتدليل. يختار شريكك رقمًا دون رؤية الخيارات.", ChallengeType.SECRET, Boldness.PRIVATE, 46, 0, "مفاجأة", setOf(NightMood.SPICY, NightMood.PLAYFUL), true),
        Challenge(105, "الليلة القادمة", "خططا لساعة خاصة في موعد لاحق: متى تبدأ، ما الأجواء، وما الشيء الذي يريد كل طرف أن يشعر به.", ChallengeType.TOGETHER, Boldness.PRIVATE, 42, 0, "موعد", setOf(NightMood.SPICY, NightMood.DISCOVERY)),
        Challenge(106, "ما الذي يسبق القرب؟", "رتّبا ما يساعدكما أكثر على القرب: كلام خاص، موسيقى، ضحك، لمسة، أو هدوء. قارنا الترتيب دون جدال.", ChallengeType.DUEL, Boldness.PRIVATE, 42, 0, "اكتشاف", setOf(NightMood.DISCOVERY, NightMood.SPICY)),
        Challenge(107, "أريد منك الليلة…", "أكمل الجملة بطلب واحد واضح وراقٍ. يمكن لشريكك أن يقول: نعم، لاحقًا، أو أقترح بديلًا.", ChallengeType.SECRET, Boldness.PRIVATE, 46, 0, "طلب", setOf(NightMood.SPICY), true),
        Challenge(108, "لا تضحك", "اقتربا، حافظا على التواصل البصري 60 ثانية، وكل طرف يحاول إرباك الآخر بابتسامة أو همسة فقط.", ChallengeType.DUEL, Boldness.PRIVATE, 44, 60, "مرح", setOf(NightMood.SPICY, NightMood.PLAYFUL)),
        Challenge(109, "لمسة بإذن", "اسأل شريكك: أين تحب لمسة هادئة الآن؟ اليد، الوجه، الشعر، الكتف، أو الظهر. نفّذ اختياره فقط.", ChallengeType.SOLO, Boldness.PRIVATE, 44, 45, "لمسة", setOf(NightMood.SPICY, NightMood.ROMANTIC)),
        Challenge(110, "حضن من الخلف", "اسأل أولًا، ثم قدّم حضنًا هادئًا من الخلف لمدة 30 ثانية. يمكن تغيير الوضع أو التوقف فورًا.", ChallengeType.SOLO, Boldness.PRIVATE, 44, 30, "قرب", setOf(NightMood.SPICY, NightMood.ROMANTIC)),
        Challenge(111, "قبلة من اختياركما", "اللعبة لا تحدد نوع القبلة. اتفقا عليها بينكما ونفّذاها فقط إذا قال الطرفان نعم بوضوح.", ChallengeType.TOGETHER, Boldness.PRIVATE, 48, 0, "قبلة", setOf(NightMood.SPICY), true),
        Challenge(112, "ما الذي يشعل القرب؟", "كل طرف يذكر شيئًا واحدًا غير صريح يجعله أكثر استعدادًا للقرب: كلمة، نظرة، عطر، أجواء، أو مبادرة.", ChallengeType.SECRET, Boldness.PRIVATE, 44, 0, "رغبة", setOf(NightMood.SPICY, NightMood.DISCOVERY), true),
        Challenge(113, "دقيقة إطراء خاص", "لديك دقيقة كاملة لتصف ما يعجبك في حضور شريكك وقربه دون تكرار كلمة واحدة.", ChallengeType.SOLO, Boldness.PRIVATE, 44, 60, "مغازلة", setOf(NightMood.SPICY, NightMood.ROMANTIC)),
        Challenge(114, "صوت قريب", "يغمض شريكك عينيه، وتهمس له بثلاث جمل: مجاملة، ذكرى، ورغبة لطيفة لوقت لاحق.", ChallengeType.SOLO, Boldness.PRIVATE, 46, 0, "حواس", setOf(NightMood.SPICY, NightMood.DISCOVERY), true),
        Challenge(115, "إيقاع واحد", "اجلسا قريبين، أمسكا اليدين، وحاولا توحيد التنفس لمدة 90 ثانية مع إبقاء المسافة مريحة.", ChallengeType.TOGETHER, Boldness.PRIVATE, 46, 90, "تناغم", setOf(NightMood.SPICY, NightMood.ROMANTIC)),
        Challenge(116, "حد ورغبة", "كل طرف يشارك حدًا يريد احترامه ورغبة يريد فهمها. لا تنفيذ الآن؛ الهدف هو الأمان والثقة.", ChallengeType.SECRET, Boldness.PRIVATE, 48, 0, "ثقة", setOf(NightMood.DISCOVERY, NightMood.SPICY), true),
        Challenge(117, "كلمة الأمان", "اختارا كلمة لطيفة تعني: توقف أو بدّل البطاقة. استخدماها تجريبيًا مرة لتأكيد أن التوقف مقبول بلا تفسير.", ChallengeType.TOGETHER, Boldness.PRIVATE, 42, 0, "أمان", setOf(NightMood.SPICY, NightMood.DISCOVERY)),
        Challenge(118, "رغبة مؤجلة", "اكتب رغبة زوجية خاصة لا تريد تنفيذها الآن. اختارا موعدًا مناسبًا للحديث عنها فقط، لا لتنفيذها.", ChallengeType.SECRET, Boldness.PRIVATE, 46, 0, "رغبة", setOf(NightMood.DISCOVERY, NightMood.SPICY), true),
        Challenge(119, "اتبع الإشارة", "أحدكما يوجّه الآخر بالإشارات لاختيار المكان والمسافة وطريقة القرب. بعد دقيقة بدّلا الأدوار.", ChallengeType.TOGETHER, Boldness.PRIVATE, 48, 120, "لعبة", setOf(NightMood.SPICY, NightMood.PLAYFUL)),
        Challenge(120, "الختام الخاص", "اختارا ختام الجولة بعيدًا عن الشاشة: حديث خاص، موسيقى وقرب، تدليل، أو بطاقة من صنعكما. لا نقاط بعد الآن.", ChallengeType.TOGETHER, Boldness.PRIVATE, 50, 0, "ختام", setOf(NightMood.SPICY, NightMood.ROMANTIC), true)
'''.strip()
if marker not in text:
    raise SystemExit('Challenge 90 marker not found')
text = text.replace(marker, marker + ',\n' + private_cards)
bank.write_text(text, encoding='utf-8')

tests.mkdir(parents=True, exist_ok=True)
(tests / 'ChallengeBankTest.kt').write_text('''package com.baynana.couplesgame

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChallengeBankTest {
    @Test
    fun bankHasOneHundredTwentyUniqueCards() {
        assertEquals(120, ChallengeBank.all.size)
        assertEquals(120, ChallengeBank.all.map { it.id }.toSet().size)
    }

    @Test
    fun privateRoundUsesAllowedLevelAndMood() {
        val round = ChallengeBank.createRound(Boldness.PRIVATE, NightMood.SPICY, 20, seed = 77)
        assertEquals(20, round.size)
        assertTrue(round.all { it.boldness.rank <= Boldness.PRIVATE.rank })
        assertTrue(round.all { NightMood.SPICY in it.moods || it.moods.isEmpty() })
        assertTrue(round.any { it.boldness == Boldness.PRIVATE })
    }

    @Test
    fun gentleRoundExcludesPrivateLevels() {
        val round = ChallengeBank.createRound(Boldness.GENTLE, NightMood.PLAYFUL, 10, seed = 9)
        assertTrue(round.all { it.boldness == Boldness.GENTLE })
        assertTrue(round.none { it.boldness.rank >= Boldness.INTIMATE.rank })
    }
}
''', encoding='utf-8')

gradle = root / 'app/build.gradle.kts'
text = gradle.read_text(encoding='utf-8')
text = re.sub(r'versionCode\s*=\s*\d+', 'versionCode = 8', text)
text = re.sub(r'versionName\s*=\s*"[^"]+"', 'versionName = "0.8.0"', text)
gradle.write_text(text, encoding='utf-8')

print('Baynana v0.8.0 patch applied: launcher fixed and 120-card bank enabled.')
