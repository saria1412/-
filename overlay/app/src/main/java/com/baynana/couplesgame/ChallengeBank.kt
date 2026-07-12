package com.baynana.couplesgame

object ChallengeBank {
    val all: List<Challenge> = listOf(
        Challenge(1, "مجاملة لا تتكرر", "قل لشريكك مجاملة جديدة لم تقلها له من قبل، واذكر سببًا حقيقيًا وراءها.", ChallengeType.SOLO, Boldness.GENTLE, 10, category = "تقارب"),
        Challenge(2, "نظرة وابتسامة", "انظرا إلى بعضكما لمدة 20 ثانية. أول من يضحك يمنح الآخر قلبين إضافيين.", ChallengeType.DUEL, Boldness.GENTLE, 12, 20, "مرح"),
        Challenge(3, "قلّدني بلطف", "قلّد حركة أو عبارة مشهورة لشريكك دون سخرية. عليه أن يخمّنها.", ChallengeType.SOLO, Boldness.GENTLE, 10, category = "ضحك"),
        Challenge(4, "أجمل ذكرى", "اختارا معًا أجمل ذكرى بينكما، وكل طرف يشرح لماذا اختارها.", ChallengeType.TOGETHER, Boldness.GENTLE, 14, category = "ذكريات"),
        Challenge(5, "أغنيتنا الآن", "اختر أغنية تعبّر عن مزاجكما الحالي وشغّل منها 30 ثانية.", ChallengeType.SOLO, Boldness.GENTLE, 10, 30, "موسيقى"),
        Challenge(6, "رسالة من خمس كلمات", "اكتب لشريكك رسالة ودية من خمس كلمات فقط واقرأها بصوتك.", ChallengeType.SECRET, Boldness.GENTLE, 12, category = "تواصل"),
        Challenge(7, "اسم دلع جديد", "اختر اسم دلع جديدًا ومهذبًا لشريكك، وله حق الاعتماد أو الرفض بابتسامة.", ChallengeType.SOLO, Boldness.GENTLE, 10, category = "مرح"),
        Challenge(8, "ثلاثة أشياء", "اذكر ثلاثة أشياء صغيرة يفعلها شريكك وتجعل يومك أفضل.", ChallengeType.SOLO, Boldness.GENTLE, 12, category = "امتنان"),
        Challenge(9, "تحدي الصورة", "التقطا صورة عفوية مضحكة معًا من دون إعادة التصوير.", ChallengeType.TOGETHER, Boldness.GENTLE, 12, category = "مرح"),
        Challenge(10, "هدية خيالية", "لو كانت الميزانية مفتوحة، ما الهدية التي ستختارها لشريكك؟ فسّر اختيارك.", ChallengeType.SOLO, Boldness.GENTLE, 10, category = "خيال"),
        Challenge(11, "موعد اقتصادي", "خططا لموعد ممتع بميزانية 50 ريالًا فقط.", ChallengeType.TOGETHER, Boldness.GENTLE, 14, category = "موعد"),
        Challenge(12, "شكرًا على...", "أكمل الجملة: شكرًا لأنك دائمًا... مع مثال من موقف حقيقي.", ChallengeType.SOLO, Boldness.GENTLE, 12, category = "امتنان"),
        Challenge(13, "رقصة سريعة", "ارقصا معًا 30 ثانية على أول أغنية تظهر، دون الاهتمام بالإتقان.", ChallengeType.TOGETHER, Boldness.GENTLE, 14, 30, "مرح"),
        Challenge(14, "سؤال البرق", "لكل طرف 20 ثانية ليسأل الآخر ثلاثة أسئلة سريعة ومضحكة.", ChallengeType.DUEL, Boldness.GENTLE, 12, 20, "أسئلة"),
        Challenge(15, "اختر لي", "اختر لشريكك مشروبًا أو حلوى تتوقع أنها تناسب مزاجه الآن.", ChallengeType.SOLO, Boldness.GENTLE, 10, category = "ذوق"),
        Challenge(16, "إعادة المشهد", "مثّلا موقفًا مضحكًا حدث بينكما، لكن تبادلا الأدوار.", ChallengeType.TOGETHER, Boldness.GENTLE, 14, category = "تمثيل"),
        Challenge(17, "صفة سرية", "اكتب صفة جميلة في شريكك، واجعله يخمّن ما كتبت.", ChallengeType.SECRET, Boldness.GENTLE, 12, category = "تقارب"),
        Challenge(18, "دقيقة بلا هواتف", "ضعا الهاتف جانبًا لدقيقة وتحدثا عن أفضل شيء حدث هذا الأسبوع.", ChallengeType.TOGETHER, Boldness.GENTLE, 14, 60, "تواصل"),
        Challenge(19, "اختيار الفيلم", "لكل طرف 15 ثانية للدفاع عن فيلم يريد مشاهدته. الفائز يختار.", ChallengeType.DUEL, Boldness.GENTLE, 12, 15, "مرح"),
        Challenge(20, "وعد صغير", "قدّم وعدًا بسيطًا وقابلًا للتنفيذ خلال الأيام السبعة القادمة.", ChallengeType.SOLO, Boldness.GENTLE, 12, category = "اهتمام"),

        Challenge(21, "همسة خاصة", "اقترب وهمس لشريكك بعبارة إعجاب صادقة لا يسمعها سواه.", ChallengeType.SOLO, Boldness.BOLD, 18, category = "مغازلة"),
        Challenge(22, "حضن هادئ", "تبادلا حضنًا لمدة 30 ثانية دون كلام. يمكن التوقف في أي لحظة.", ChallengeType.TOGETHER, Boldness.BOLD, 20, 30, "قرب"),
        Challenge(23, "أكثر ما يجذبني", "اذكر لشريكك صفة واحدة في حضوره أو شخصيته تجذبك حتى الآن.", ChallengeType.SOLO, Boldness.BOLD, 18, category = "مغازلة"),
        Challenge(24, "رقصة بطيئة", "اختارا مقطعًا موسيقيًا وارقصا ببطء لمدة دقيقة.", ChallengeType.TOGETHER, Boldness.BOLD, 22, 60, "قرب"),
        Challenge(25, "مواجهة النظرات", "اقتربا واجعلا المسافة مريحة. أول من يبعد نظره يخسر المواجهة.", ChallengeType.DUEL, Boldness.BOLD, 20, 30, "جرأة"),
        Challenge(26, "قبلة تقدير", "اختر قبلة تقدير على الجبين أو اليد أو الخد، بعد موافقة شريكك.", ChallengeType.SOLO, Boldness.BOLD, 18, category = "مودة"),
        Challenge(27, "طلب دلع", "اطلب من شريكك شيئًا لطيفًا يسعدك اليوم: كلمة، حضن، مشروب أو وقت خاص.", ChallengeType.SECRET, Boldness.BOLD, 18, category = "اهتمام"),
        Challenge(28, "مدح قريب", "اقترب وقل ثلاث كلمات تصف جمال شريكك من وجهة نظرك.", ChallengeType.SOLO, Boldness.BOLD, 18, category = "مغازلة"),
        Challenge(29, "تدليك كتفين", "قدّم تدليكًا خفيفًا للكتفين لمدة دقيقة، مع التأكد من الراحة.", ChallengeType.SOLO, Boldness.BOLD, 20, 60, "استرخاء"),
        Challenge(30, "سر لطيف", "شارك شريكك شيئًا لطيفًا أعجبك فيه مؤخرًا ولم تخبره به.", ChallengeType.SECRET, Boldness.BOLD, 18, category = "تقارب"),
        Challenge(31, "إعادة أول لقاء", "مثّلا أول دقيقة من أول لقاء أو أول حديث بينكما بطريقتكما الحالية.", ChallengeType.TOGETHER, Boldness.BOLD, 20, category = "ذكريات"),
        Challenge(32, "جملة في الأذن", "همس بجملة تبدأ بـ: أحب قربك عندما...", ChallengeType.SOLO, Boldness.BOLD, 18, category = "مغازلة"),
        Challenge(33, "اختيار عطر", "اقترب من عطر شريكك أو اختَر له رائحة تتمنى أن يستخدمها في موعدكما القادم.", ChallengeType.SOLO, Boldness.BOLD, 16, category = "ذوق"),
        Challenge(34, "دقيقة ممنوع الكلام", "عبّرا عن الاهتمام لمدة دقيقة بالإشارات والابتسامة فقط.", ChallengeType.TOGETHER, Boldness.BOLD, 20, 60, "مرح"),
        Challenge(35, "موعد مفاجئ", "خطط سرًا لتفصيل واحد جريء ولطيف في موعدكما القادم، ثم اكشفه الآن.", ChallengeType.SECRET, Boldness.BOLD, 20, category = "موعد"),
        Challenge(36, "لقطة رومانسية", "اختارا وضعية تصوير رومانسية راقية والتقطا صورة واحدة فقط.", ChallengeType.TOGETHER, Boldness.BOLD, 20, category = "ذكرى"),
        Challenge(37, "أقرب مجاملة", "كل طرف يقول أكثر تفصيل صغير يحبه في وجه الآخر. الأصدق يفوز.", ChallengeType.DUEL, Boldness.BOLD, 20, category = "مغازلة"),
        Challenge(38, "وعد القرب", "اختارا وقتًا محددًا هذا الأسبوع يكون لكما فقط دون أعمال أو هواتف.", ChallengeType.TOGETHER, Boldness.BOLD, 20, category = "اهتمام"),
        Challenge(39, "اختيار اللقب", "اختر لقبًا رومانسيًا لشريكك لمدة بقية الجولة، وله حق تبديله.", ChallengeType.SOLO, Boldness.BOLD, 16, category = "مرح"),
        Challenge(40, "تحدي النبض", "ضعا اليدين فوق بعضهما لمدة 30 ثانية وحاولا التنفس بالوتيرة نفسها.", ChallengeType.TOGETHER, Boldness.BOLD, 20, 30, "قرب"),
        Challenge(41, "رسالة صوتية", "سجّل رسالة إعجاب قصيرة لشريكك بينما هو أمامك، واحفظها له.", ChallengeType.SOLO, Boldness.BOLD, 18, category = "مغازلة"),
        Challenge(42, "أمنية قريبة", "أكمل: أتمنى أن نكرر قريبًا... واختر ذكرى أو موعدًا تحبانه.", ChallengeType.SOLO, Boldness.BOLD, 18, category = "رغبة"),

        Challenge(43, "قبلة يختارها الاثنان", "اختارا معًا قبلة مريحة لكليكما. لا تُنفذ إلا بموافقة واضحة من الطرفين.", ChallengeType.TOGETHER, Boldness.BOLDER, 28, category = "حميمية"),
        Challenge(44, "أقرب لحظة", "اجلسا قريبين وانظرا لبعضكما 45 ثانية دون كلام. الابتسامة مسموحة.", ChallengeType.TOGETHER, Boldness.BOLDER, 26, 45, "قرب"),
        Challenge(45, "بطاقة طلب واحد", "اكتب طلبًا رومانسيًا واحدًا ترغب به الليلة. يحق للطرف الآخر القبول أو التعديل أو التبديل.", ChallengeType.SECRET, Boldness.BOLDER, 28, category = "رغبة"),
        Challenge(46, "همسة جريئة", "همس لشريكك بأجرأ مجاملة راقية تستطيع قولها الآن.", ChallengeType.SOLO, Boldness.BOLDER, 26, category = "مغازلة"),
        Challenge(47, "اختيار لمسة مريحة", "اطلب من شريكك أن يختار لمسة مريحة له: يد، كتف، شعر أو حضن، ثم نفذها بلطف.", ChallengeType.SOLO, Boldness.BOLDER, 26, 30, "حميمية"),
        Challenge(48, "رقصة أقرب", "ارقصا ببطء لمدة دقيقة بالطريقة التي تشعركما بالراحة والقرب.", ChallengeType.TOGETHER, Boldness.BOLDER, 30, 60, "حميمية"),
        Challenge(49, "أتمنى الليلة...", "أكمل الجملة بطلب رومانسي غير إلزامي، ثم اسأل شريكك عن حدوده وراحته.", ChallengeType.SECRET, Boldness.BOLDER, 28, category = "تواصل"),
        Challenge(50, "تحدي القرب الصامت", "ابقيا قريبين لمدة دقيقة؛ الكلام ممنوع، والانسحاب مسموح دون سؤال.", ChallengeType.TOGETHER, Boldness.BOLDER, 28, 60, "قرب"),
        Challenge(51, "اختيار القبلة", "اختر من: الجبين، الخد، اليد أو قبلة يحددها الطرفان. شريكك يختار.", ChallengeType.SOLO, Boldness.BOLDER, 26, category = "مودة"),
        Challenge(52, "سر انجذاب", "شارك شريكك تفصيلًا في حضوره أو أسلوبه يجعلك تنجذب إليه أكثر.", ChallengeType.SECRET, Boldness.BOLDER, 28, category = "مغازلة"),
        Challenge(53, "رهان العيون", "مواجهة 45 ثانية عن قرب. الفائز يختار تحديًا لطيفًا للطرف الآخر.", ChallengeType.DUEL, Boldness.BOLDER, 28, 45, "جرأة"),
        Challenge(54, "دقيقة تدليل", "لديك دقيقة لتدلل شريكك بالطريقة التي يطلبها ضمن الحدود المريحة.", ChallengeType.SOLO, Boldness.BOLDER, 30, 60, "اهتمام"),
        Challenge(55, "موعد خلف الباب", "خططا لموعد خاص في المنزل: إضاءة، أغنية، مشروب، ووقت بلا هواتف.", ChallengeType.TOGETHER, Boldness.BOLDER, 30, category = "موعد"),
        Challenge(56, "الطلب السري", "اكتب رغبة زوجية راقية لوقت لاحق. اكشفها فقط إن شعرتما بالراحة.", ChallengeType.SECRET, Boldness.BOLDER, 28, category = "رغبة"),
        Challenge(57, "مفاجأة في 60 ثانية", "لكل طرف دقيقة ليحضّر مفاجأة صغيرة: عبارة، حركة، أغنية أو حضن.", ChallengeType.DUEL, Boldness.BOLDER, 30, 60, "مفاجأة"),
        Challenge(58, "تبادل الأدوار", "كل طرف يصف كيف يحب أن يبدأ شريكه لحظة رومانسية مثالية.", ChallengeType.TOGETHER, Boldness.BOLDER, 28, category = "تواصل"),
        Challenge(59, "نقطة ضعف جميلة", "قل لشريكك بلطف ما الحركة أو النظرة التي تضعف مقاومتك لابتسامته.", ChallengeType.SOLO, Boldness.BOLDER, 26, category = "مغازلة"),
        Challenge(60, "بطاقة من اختياركما", "ابتكرا تحديًا جريئًا وودودًا معًا. لا يعتمد إلا بعد أن يقول الطرفان: موافق.", ChallengeType.TOGETHER, Boldness.BOLDER, 32, category = "مفاجأة")
    )

    fun createRound(level: Boldness, count: Int, seed: Int = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()): List<Challenge> {
        val allowed = all.filter { it.boldness.rank <= level.rank }
        val random = kotlin.random.Random(seed)
        val groups = allowed.groupBy { it.type }
        val selected = mutableListOf<Challenge>()
        listOf(ChallengeType.TOGETHER, ChallengeType.DUEL, ChallengeType.SECRET).forEach { type ->
            groups[type]?.randomOrNull(random)?.let(selected::add)
        }
        allowed.shuffled(random).forEach { challenge ->
            if (selected.size < count && selected.none { it.id == challenge.id }) selected += challenge
        }
        return selected.shuffled(random).take(count)
    }
}
