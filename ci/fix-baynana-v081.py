from __future__ import annotations

import re
import sys
from pathlib import Path


def replace_once(text: str, old: str, new: str, label: str) -> str:
    if old not in text:
        raise RuntimeError(f"Could not find patch anchor: {label}")
    return text.replace(old, new, 1)


def main() -> None:
    root = Path(sys.argv[1] if len(sys.argv) > 1 else "BaynanaAndroid")
    app_file = root / "app/src/main/java/com/baynana/couplesgame/BaynanaApp.kt"
    gradle_file = root / "app/build.gradle.kts"

    text = app_file.read_text(encoding="utf-8")

    text = replace_once(
        text,
        "import androidx.compose.foundation.layout.height\n",
        "import androidx.compose.foundation.layout.height\nimport androidx.compose.foundation.layout.heightIn\n",
        "heightIn import",
    )

    text = replace_once(
        text,
        """    val haptic = LocalHapticFeedback.current

    LaunchedEffect(timerRunning, currentIndex) {""",
        """    val haptic = LocalHapticFeedback.current
    val playScrollState = rememberScrollState()

    LaunchedEffect(currentIndex) {
        playScrollState.scrollTo(0)
    }

    LaunchedEffect(timerRunning, currentIndex) {""",
        "play scroll state",
    )

    text = replace_once(
        text,
        """        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 17.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {""",
        """        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(playScrollState)
                .padding(horizontal = 17.dp, vertical = 9.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {""",
        "scrollable play column",
    )

    text = replace_once(
        text,
        'modifier = Modifier.weight(1f).fillMaxWidth(),\n                label = "challenge"',
        'modifier = Modifier.fillMaxWidth(),\n                label = "challenge"',
        "animated content height",
    )
    text = replace_once(
        text,
        "modifier = Modifier.fillMaxSize().scale(glow),",
        "modifier = Modifier.fillMaxWidth().scale(glow),",
        "challenge card height",
    )

    text = replace_once(
        text,
        """                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Button(onClick = { onComplete(0, false, useSpark, reaction) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text(nameOne, maxLines = 1) }
                        Button(onClick = { onComplete(1, false, useSpark, reaction) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) { Text(nameTwo, maxLines = 1) }
                        FilledTonalButton(onClick = { onComplete(null, false, useSpark, reaction) }, modifier = Modifier.weight(.8f), shape = RoundedCornerShape(16.dp)) { Text("تعادل") }
                    }""",
        """                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onComplete(0, false, useSpark, reaction) },
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text(nameOne, maxLines = 2, textAlign = TextAlign.Center) }
                        Button(
                            onClick = { onComplete(1, false, useSpark, reaction) },
                            modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) { Text(nameTwo, maxLines = 2, textAlign = TextAlign.Center) }
                    }
                    Spacer(Modifier.height(7.dp))
                    FilledTonalButton(
                        onClick = { onComplete(null, false, useSpark, reaction) },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("تعادل") }""",
        "duel buttons",
    )

    text = replace_once(
        text,
        """                OutlinedButton(onClick = onSafeSkip, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
                    Text("بديل ألطف — بلا سؤال ولا عقوبة  🌿", color = Color.White)
                }
            }
        }
    }
}""",
        """                OutlinedButton(onClick = onSafeSkip, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp), shape = RoundedCornerShape(16.dp)) {
                    Text("بديل ألطف — بلا سؤال ولا عقوبة  🌿", color = Color.White, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}""",
        "bottom safe area",
    )

    text = replace_once(
        text,
        "    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Velvet, DeepWine))).padding(24.dp), contentAlignment = Alignment.Center) {",
        """    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 430.dp)
            .background(Brush.verticalGradient(listOf(Velvet, DeepWine)))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {""",
        "secret card responsive height",
    )
    text = replace_once(
        text,
        "modifier = Modifier.fillMaxSize().padding(22.dp),",
        "modifier = Modifier.fillMaxWidth().padding(22.dp),",
        "challenge content width",
    )

    text = replace_once(
        text,
        """        Surface(
            color = if (challenge.boldness.rank >= Boldness.INTIMATE.rank) Wine.copy(alpha = .14f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = .72f),
            shape = RoundedCornerShape(23.dp)
        ) {
            Text(challenge.prompt, modifier = Modifier.padding(20.dp), fontSize = 19.sp, lineHeight = 29.sp, textAlign = TextAlign.Center)
        }
        Spacer(Modifier.weight(1f))""",
        """        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = if (challenge.boldness.rank >= Boldness.INTIMATE.rank) Wine.copy(alpha = .14f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = .72f),
            shape = RoundedCornerShape(23.dp)
        ) {
            Text(
                challenge.prompt,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
                fontSize = 18.sp,
                lineHeight = 27.sp,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(18.dp))""",
        "prompt wrapping",
    )

    if ".verticalScroll(playScrollState)" not in text:
        raise RuntimeError("Scrolling fix missing")
    challenge_section = text[text.index("private fun ChallengeContent"):]
    if "Spacer(Modifier.weight(1f))" in challenge_section:
        raise RuntimeError("Weighted spacer remains in challenge content")

    app_file.write_text(text, encoding="utf-8")

    gradle = gradle_file.read_text(encoding="utf-8")
    gradle = re.sub(r"versionCode\s*=\s*\d+", "versionCode = 9", gradle)
    gradle = re.sub(r'versionName\s*=\s*"[^"]+"', 'versionName = "0.8.1"', gradle)
    gradle_file.write_text(gradle, encoding="utf-8")

    print("Baynana v0.8.1 responsive fix applied successfully")


if __name__ == "__main__":
    main()
