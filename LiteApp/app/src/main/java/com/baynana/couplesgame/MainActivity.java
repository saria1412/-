package com.baynana.couplesgame;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends Activity {
    private final int burgundy = Color.rgb(123,63,82);
    private final int cream = Color.rgb(250,246,241);
    private final int ink = Color.rgb(41,36,38);
    private final int green = Color.rgb(95,140,118);
    private String p1 = "الزوج", p2 = "الزوجة";
    private int stage = 0, index = 0, count = 10;
    private int[][] answers;
    private List<Question> round;

    private static class Question {
        String category, text;
        String[] options;
        Question(String c, String t, String... o){category=c;text=t;options=o;}
    }

    private final Question[] bank = new Question[]{
        new Question("الحياة اليومية","ما الوقت المفضل للاستيقاظ في يوم الإجازة؟","مبكرًا","بين 8 و10","بعد الظهر","حسب الظروف"),
        new Question("الطعام","أي نوع من الوجبات يفضله غالبًا؟","طعام منزلي","وجبات سريعة","مطاعم جديدة","أكلات شعبية"),
        new Question("الشخصية","كيف يتخذ القرارات المهمة؟","بسرعة","بعد تفكير طويل","بعد التشاور","حسب إحساسه"),
        new Question("التواصل","عندما يكون منزعجًا، ماذا يفضل؟","أن يبقى وحده قليلًا","أن نتحدث فورًا","الاستماع دون نصائح","مبادرة لطيفة"),
        new Question("السفر","أي رحلة يفضل؟","شاطئ واسترخاء","مدينة وتسوق","جبال وطبيعة","مغامرة وتجارب"),
        new Question("المشاعر","ما أكثر شيء يجعله يشعر بالتقدير؟","كلمات الثناء","المساعدة العملية","الوقت الخاص","الهدايا"),
        new Question("المستقبل","ما الأولوية الأهم خلال السنوات القادمة؟","الاستقرار والمنزل","العمل والمشروع","السفر والتجارب","الادخار والاستثمار"),
        new Question("المرح","كيف يقضي ساعتين من وقت الفراغ؟","مشاهدة فيلم","الخروج","النوم والراحة","هواية أو لعبة"),
        new Question("الخلافات","ما أفضل وقت لمناقشة مشكلة؟","فورًا","بعد الهدوء","في نهاية اليوم","بعد ترتيب الأفكار"),
        new Question("الهدايا","ما نوع الهدية الأقرب لذوقه؟","شيء عملي","تجربة أو رحلة","عطر أو ملابس","هدية رمزية"),
        new Question("المنزل","ما أكثر شيء يهمه في المنزل؟","النظافة","الترتيب","الهدوء","الديكور"),
        new Question("الذكريات","أي ذكرى يفضل استعادتها؟","أول لقاء","رحلة مشتركة","مناسبة عائلية","موقف مضحك"),
        new Question("العمل","ماذا يحتاج بعد يوم عمل مرهق؟","راحة وهدوء","حديث وفضفضة","وجبة يحبها","خروج قصير"),
        new Question("المال","عند توفر مبلغ إضافي، ماذا يفضل؟","الادخار","شراء شيء مطلوب","السفر","الاستثمار"),
        new Question("العائلة","أي نشاط عائلي يفضله؟","زيارة الأقارب","نزهة خارجية","جلسة منزلية","مطعم"),
        new Question("الشخصية","ما أكثر ما يزعجه؟","التأخير","الفوضى","التجاهل","الانتقاد"),
        new Question("الرومانسية","ما المبادرة التي تسعده أكثر؟","رسالة لطيفة","موعد مفاجئ","هدية بسيطة","وقت دون جوالات"),
        new Question("الطعام","ماذا يختار غالبًا؟","حلو","مالح","قهوة","عصير"),
        new Question("المستقبل","ما المهارة التي يرغب في تطويرها؟","مهنية","مالية","لغوية","رياضية"),
        new Question("التواصل","كيف يفضل الاعتذار؟","كلمات واضحة","تصرف عملي","وقت ثم نقاش","مبادرة وهدية")
    };

    @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(cream); showWelcome();}

    private LinearLayout base(){
        LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setGravity(Gravity.RIGHT); l.setPadding(dp(22),dp(22),dp(22),dp(30)); l.setBackgroundColor(cream); l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL); return l;
    }
    private TextView text(String s,int size,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(ink);v.setGravity(Gravity.RIGHT);v.setPadding(0,dp(8),0,dp(8));if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(17);b.setTextColor(Color.WHITE);b.setBackgroundColor(burgundy);b.setAllCaps(false);b.setMinHeight(dp(56));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(8),0,dp(8));b.setLayoutParams(p);return b;}
    private EditText input(String hint){EditText e=new EditText(this);e.setHint(hint);e.setTextSize(18);e.setTextColor(ink);e.setSingleLine();e.setGravity(Gravity.RIGHT);e.setInputType(InputType.TYPE_CLASS_TEXT);e.setPadding(dp(14),dp(12),dp(14),dp(12));e.setBackgroundColor(Color.WHITE);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(58));p.setMargins(0,dp(7),0,dp(7));e.setLayoutParams(p);return e;}
    private void show(LinearLayout l){ScrollView s=new ScrollView(this);s.addView(l,new ScrollView.LayoutParams(-1,-2));setContentView(s);}

    private void showWelcome(){
        LinearLayout l=base();TextView logo=text("بـيـنـنـا",38,true);logo.setTextColor(burgundy);logo.setGravity(Gravity.CENTER);l.addView(logo);TextView sub=text("اكتشفوا بعضكم أكثر",18,false);sub.setGravity(Gravity.CENTER);l.addView(sub);l.addView(text("لعبة خاصة للزوجين تقارن الإجابات الحقيقية بتوقعات الشريك.",18,false));
        EditText a=input("اسم الطرف الأول");EditText b=input("اسم الطرف الثاني");l.addView(a);l.addView(b);
        l.addView(text("عدد أسئلة الجولة",17,true));LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);row.setGravity(Gravity.CENTER);for(int n:new int[]{5,10,15}){Button x=button(String.valueOf(n));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(52),1);p.setMargins(dp(4),dp(5),dp(4),dp(5));x.setLayoutParams(p);x.setOnClickListener(v->{count=n;});row.addView(x);}l.addView(row);
        Button start=button("ابدآ الجولة");start.setOnClickListener(v->{if(!a.getText().toString().trim().isEmpty())p1=a.getText().toString().trim();if(!b.getText().toString().trim().isEmpty())p2=b.getText().toString().trim();startRound();});l.addView(start);show(l);
    }

    private void startRound(){List<Question> q=new ArrayList<>();Collections.addAll(q,bank);Collections.shuffle(q);round=q.subList(0,Math.min(count,q.size()));answers=new int[4][round.size()];for(int i=0;i<4;i++)for(int j=0;j<round.size();j++)answers[i][j]=-1;stage=0;index=0;showHandoff();}
    private String actor(){return stage==0||stage==3?p1:p2;}
    private String stageTitle(){if(stage==0)return p1+" يجيب عن نفسه";if(stage==1)return p2+" يتوقع إجابات "+p1;if(stage==2)return p2+" يجيب عن نفسه";return p1+" يتوقع إجابات "+p2;}
    private void showHandoff(){LinearLayout l=base();TextView h=text("مرّر الجوال إلى",20,false);h.setGravity(Gravity.CENTER);l.addView(h);TextView n=text(actor(),34,true);n.setTextColor(burgundy);n.setGravity(Gravity.CENTER);l.addView(n);l.addView(text(stageTitle(),22,true));l.addView(text("الإجابات السابقة مخفية. لا تضغط متابعة إلا بعد استلام الشخص الصحيح للجوال.",16,false));Button go=button("أنا جاهز — ابدأ");go.setOnClickListener(v->showQuestion());l.addView(go);show(l);}
    private String prompt(Question q){if(stage==0||stage==2)return q.text;return "بحسب معرفتك بشريكك: "+q.text;}
    private void showQuestion(){Question q=round.get(index);LinearLayout l=base();l.addView(text(q.category+"  •  "+(index+1)+" من "+round.size(),15,true));TextView t=text(prompt(q),24,true);t.setPadding(0,dp(20),0,dp(18));l.addView(t);for(int i=0;i<q.options.length;i++){final int selected=i;Button b=button(q.options[i]);b.setTextColor(ink);b.setBackgroundColor(Color.WHITE);b.setOnClickListener(v->{answers[stage][index]=selected;next();});l.addView(b);}show(l);}
    private void next(){index++;if(index<round.size()){showQuestion();return;}stage++;index=0;if(stage<4)showHandoff();else showResults();}
    private int score(int selfStage,int guessStage){int s=0;for(int i=0;i<round.size();i++)if(answers[selfStage][i]==answers[guessStage][i])s++;return Math.round(s*100f/round.size());}
    private void showResults(){int p2KnowsP1=score(0,1);int p1KnowsP2=score(2,3);int avg=(p1KnowsP2+p2KnowsP1)/2;LinearLayout l=base();TextView h=text("نتيجة الجولة",32,true);h.setTextColor(burgundy);h.setGravity(Gravity.CENTER);l.addView(h);l.addView(resultCard("معرفة "+p1+" بـ "+p2,p1KnowsP2));l.addView(resultCard("معرفة "+p2+" بـ "+p1,p2KnowsP1));l.addView(resultCard("المعرفة المشتركة",avg));l.addView(text("إجابات تستحق الحوار",21,true));for(int i=0;i<round.size();i++){if(answers[0][i]!=answers[1][i]||answers[2][i]!=answers[3][i]){Question q=round.get(i);TextView d=text("• "+q.text,17,false);d.setBackgroundColor(Color.WHITE);d.setPadding(dp(14),dp(13),dp(14),dp(13));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(5),0,dp(5));d.setLayoutParams(p);l.addView(d);}}
        Button again=button("جولة جديدة");again.setOnClickListener(v->showWelcome());l.addView(again);show(l);}
    private View resultCard(String label,int value){LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setGravity(Gravity.CENTER);box.setBackgroundColor(Color.WHITE);box.setPadding(dp(16),dp(15),dp(16),dp(15));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(7),0,dp(7));box.setLayoutParams(p);TextView a=text(label,18,true);a.setGravity(Gravity.CENTER);box.addView(a);TextView b=text(value+"%",36,true);b.setTextColor(value>=70?green:burgundy);b.setGravity(Gravity.CENTER);box.addView(b);return box;}
    private int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
}
