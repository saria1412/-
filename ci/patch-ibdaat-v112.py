from pathlib import Path
import re
import sys

root = Path(sys.argv[1] if len(sys.argv) > 1 else 'IbdaatDuma_Android')

html_path = root / 'app/src/main/assets/calculator.html'
html = html_path.read_text(encoding='utf-8')
html = html.replace('    <button class="quick-btn" onclick="scrollToPriceSettings()">⚙️ الأسعار</button>\n', '')
html = html.replace('grid-template-columns:repeat(3, minmax(0,1fr));', 'grid-template-columns:repeat(2, minmax(0,1fr));')
html = html.replace('id="s50_margin" value="32"', 'id="s50_margin" value=""')
html = html.replace('id="s100_margin" value="32"', 'id="s100_margin" value=""')

luxury_css = r'''
  /* Luxury navy-and-gold theme */
  :root {
    --bg:#F6F1E8; --surface:#FFFDF8; --card:#FFFFFF; --border:#D8CCB5; --border2:#C3AA73;
    --accent:#C99A4B; --accent-lt:#F3E7CF; --accent2:#E7C788; --text:#13263F; --muted:#6F7480;
    --green:#1F7A58; --green-lt:#EEF8F2; --green-br:#1D6D50; --orange:#B87417; --orange-lt:#FFF6E8;
    --blue:#18355A; --blue-lt:#EEF3F9; --red:#B34747; --radius:18px;
    --shadow:0 10px 28px rgba(18,35,60,.08),0 3px 10px rgba(18,35,60,.05);
  }
  body{background:linear-gradient(180deg,#F6F1E8 0%,#F8F5EF 100%);color:var(--text)}
  .header{background:linear-gradient(135deg,#07172D 0%,#0F2746 58%,#183A61 100%);color:#fff;border-bottom:none;border-bottom-left-radius:34px;border-bottom-right-radius:34px;box-shadow:0 16px 34px rgba(8,19,37,.30)}
  .header-icon{background:linear-gradient(135deg,#E8C98A,#C99A4B 68%,#A8792E);box-shadow:0 8px 16px rgba(201,154,75,.35)}
  .brand-copy h1,.header h1{color:#fff}.brand-copy p,.header p{color:#E8D7B0}
  .quick-btn{border-color:rgba(232,215,176,.50);background:rgba(255,255,255,.09);color:#fff;box-shadow:inset 0 1px 0 rgba(255,255,255,.08)}
  .tabs{background:transparent;border-bottom:none;margin-top:-15px;gap:10px}
  .tab{color:var(--blue);background:rgba(255,253,248,.94);border:1.5px solid var(--border2);border-radius:16px;box-shadow:0 8px 18px rgba(18,35,60,.10);margin-bottom:0}
  .tab.active{color:#fff;border-color:transparent;background:linear-gradient(135deg,#C99A4B 0%,#E4BD78 100%);box-shadow:0 12px 24px rgba(201,154,75,.33)}
  #tab50.active,#tab100.active{background:rgba(255,253,248,.78);border:1px solid rgba(201,154,75,.18);border-radius:24px;box-shadow:0 14px 38px rgba(18,35,60,.06)}
  #tab50 .size-badge,#tab100 .size-badge{background:linear-gradient(135deg,#FFF7EA,#F4E4C2);border-color:#D6B273;color:var(--blue)}
  #tab50 .paper-box,#tab100 .paper-box{background:var(--surface);border-color:var(--border2);box-shadow:0 0 0 4px rgba(201,154,75,.10),var(--shadow)}
  #tab50 .paper-box-title,#tab100 .paper-box-title{color:var(--blue)}
  #tab50 .field-card,#tab100 .field-card,#tab50 .toggle-section,#tab100 .toggle-section{background:var(--surface);border-color:var(--border)}
  .paper-box,.field-card,.toggle-section,.results{background:var(--surface);border-color:var(--border);box-shadow:var(--shadow)}
  .paper-input,input[type="number"],select,#s50_count,#s100_count,.margin-input{background:#fff;border-color:var(--border);color:var(--text);border-radius:12px}
  .paper-input:focus,input[type="number"]:focus,select:focus,#s50_count:focus,#s100_count:focus,.margin-input:focus{border-color:var(--accent);box-shadow:0 0 0 4px rgba(201,154,75,.18)}
  .auto-value{background:linear-gradient(135deg,#FFF7EA,#FCF0D9);border-color:#E1C58D;color:var(--blue)}
  .dot{background:var(--accent);box-shadow:0 0 0 4px rgba(201,154,75,.18)}
  .auto-badge,.auto-toggle-val{background:var(--accent-lt);border-color:#E1C58D;color:var(--blue)}
  .toggle input:checked+.toggle-slider{background:linear-gradient(135deg,#C99A4B,#D8B06A)}
  .margin-row{background:linear-gradient(135deg,#FFF7EA,#F9EACC);border-color:#E0C38B}.margin-label{color:var(--blue)}
  .results-header{background:linear-gradient(135deg,#10243E 0%,#1A3960 100%);color:#fff}
  .breakdown{background:#FFFEFB;border-color:#E6D8BC}.breakdown-title{background:#F7F0E2;color:var(--blue);border-color:#E6D8BC}
  .formula-note{background:linear-gradient(135deg,#FFF9ED,#F8EFD8);border-color:#E5CAA0;color:var(--blue)}
  .mobile-actions{background:rgba(255,253,248,.96);border-color:#E3D5BC;border-radius:20px}.save-btn{background:linear-gradient(135deg,#10243E 0%,#1C3E66 55%,#C99A4B 100%)}.share-btn{border-color:var(--border2);color:var(--blue)}
'''
html = html.replace('</style>', luxury_css + '\n</style>', 1)

old_share = re.search(r'function nativeShareResult\(\)\{.*?\n\}', html, re.S)
if not old_share:
    raise RuntimeError('nativeShareResult not found')
new_share = r'''function specSide(inner, outer){
  if(inner && outer) return 'داخلي وخارجي';
  if(inner) return 'داخلي';
  if(outer) return 'خارجي';
  return 'بدون';
}
function nativeShareResult(){
  var st=currentState(), r=st.result||{};
  if(!st.count || !st.perSheet || !st.grammage || !r.sellPrice){ alert('أكمل بيانات التسعير أولًا.'); return; }
  var text='إبداعات دوما — مواصفات علب كرتون\n'
    +'المقاس: '+st.size+' سم\n'
    +'الجراماج: '+st.grammage+' جرام\n'
    +'الكمية: '+st.count.toLocaleString('en-US')+' علبة\n'
    +'عدد العلب في الشيت: '+st.perSheet+'\n'
    +'عدد الشيتات: '+fmt(r.sheets,0)+' شيت\n'
    +'الطباعة: '+specSide(st.innerPrint,st.outerPrint)+'\n'
    +'السولفان: '+specSide(st.innerSilver,st.outerSilver)+'\n'
    +'الورنيش: '+specSide(st.innerVarnish,st.outerVarnish)+'\n'
    +'نقاط التلزيق: '+(st.lzPoints>0?st.lzPoints+' نقطة':'بدون')+'\n'
    +'القالب: '+(st.newMold?'قالب جديد':'قالب متوفر / بدون قالب جديد')+'\n'
    +'أدنى سعر للبيع: '+fmt(r.sellPrice,2)+' ر.س للحبة';
  if(androidAvailable()) Android.shareResult(text);
}'''
html = html[:old_share.start()] + new_share + html[old_share.end():]
html = re.sub(r'function scheduleSettingsSave\(\)\{.*?\n\}', "function scheduleSettingsSave(){ /* لا يتم حفظ المدخلات بين الجلسات */ }", html, flags=re.S)
old_load = re.search(r'function loadNativeSettings\(data\)\{.*?\n\}', html, re.S)
if not old_load:
    raise RuntimeError('loadNativeSettings not found')
new_load = r'''function clearAllInputs(){
  ['50','100'].forEach(function(tab){
    var p='s'+tab+'_';
    [250,300,350,400].forEach(function(gr){ g(p+'p'+gr).value=''; });
    g(p+'count').value=''; g(p+'perSheet').value=''; g(p+'grammage').value='';
    g(p+'lzPoints').value='0'; g(p+'margin').value='';
    g(p+'innerPrint').checked=false; g(p+'outerPrint').checked=false;
    g(p+'innerSilver').checked=false; g(p+'outerSilver').checked=false;
    g(p+'innerVarnish').checked=false; g(p+'outerVarnish').checked=false;
    g(p+'newMold').checked=false; calc(tab);
  });
}
function loadNativeSettings(data){ clearAllInputs(); }'''
html = html[:old_load.start()] + new_load + html[old_load.end():]
html = re.sub(r'function scrollToPriceSettings\(\)\{.*?\n\}', '', html, flags=re.S)
html = html.replace("// Init\ncalc('50');\ncalc('100');\nif(androidAvailable()) Android.pageReady();", "// Init\nclearAllInputs();\nif(androidAvailable()) Android.pageReady();")
html_path.write_text(html, encoding='utf-8')

main_path = root / 'app/src/main/java/com/ibdaatduma/cartoncost/MainActivity.java'
main = main_path.read_text(encoding='utf-8')
main = main.replace('        calculatorPrefs = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);\n        projectToLoad = getIntent().getLongExtra("load_project_id", 0L);',
'''        calculatorPrefs = getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
        calculatorPrefs.edit().remove(PREFS_SETTINGS).apply();
        projectToLoad = getIntent().getLongExtra("load_project_id", 0L);''')
main = re.sub(r'    @Override\n    protected void onResume\(\) \{\n        super\.onResume\(\);\n        if \(pageReady\) injectSettingsOnly\(\);\n    \}', '    @Override\n    protected void onResume() {\n        super.onResume();\n    }', main)
main = main.replace('        stateInjected = true;\n        injectSettingsOnly();\n        if (projectToLoad > 0) {', '        stateInjected = true;\n        if (projectToLoad > 0) {')
main = main.replace('''                Toast.makeText(this, "تم تحميل المشروع: " + project.projectName, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void injectSettingsOnly() {
        String settings = calculatorPrefs.getString(PREFS_SETTINGS, DEFAULT_SETTINGS);
        evaluate("loadNativeSettings(" + JSONObject.quote(settings) + ");");
    }''', '''                Toast.makeText(this, "تم تحميل المشروع: " + project.projectName, Toast.LENGTH_SHORT).show();
            }
        } else {
            evaluate("clearAllInputs();");
        }
    }

    private void injectSettingsOnly() {
        evaluate("clearAllInputs();");
    }''')
main = re.sub(r'        @JavascriptInterface\n        public void saveCalculatorSettings\(String json\) \{\n            if \(json == null \|\| json.length\(\) > 10000\) return;\n            calculatorPrefs.edit\(\).putString\(PREFS_SETTINGS, json\).apply\(\);\n        \}', '        @JavascriptInterface\n        public void saveCalculatorSettings(String json) { }', main)
main_path.write_text(main, encoding='utf-8')

projects_path = root / 'app/src/main/java/com/ibdaatduma/cartoncost/ProjectsActivity.java'
projects_path.write_text(r'''package com.ibdaatduma.cartoncost;

import android.app.Activity; import android.app.AlertDialog; import android.content.Intent; import android.os.Bundle;
import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup; import android.widget.ArrayAdapter;
import android.widget.ListView; import android.widget.TextView; import android.widget.Toast; import org.json.JSONObject;
import java.util.ArrayList; import java.util.List;

public class ProjectsActivity extends Activity {
 private DatabaseHelper db; private final List<ProjectRecord> items=new ArrayList<>(); private ArrayAdapter<ProjectRecord> adapter; private TextView empty;
 @Override protected void onCreate(Bundle b){ super.onCreate(b); setContentView(R.layout.activity_list); db=new DatabaseHelper(this); ((TextView)findViewById(R.id.listTitle)).setText("المشاريع المحفوظة"); findViewById(R.id.btnAdd).setVisibility(View.GONE); findViewById(R.id.btnBack).setOnClickListener(v->finish()); empty=findViewById(R.id.emptyText); empty.setText("لا توجد مشاريع محفوظة بعد."); ListView list=findViewById(R.id.listView); adapter=new ArrayAdapter<ProjectRecord>(this,R.layout.item_two_line,items){ @Override public View getView(int pos,View convert,ViewGroup parent){ if(convert==null) convert=LayoutInflater.from(ProjectsActivity.this).inflate(R.layout.item_two_line,parent,false); ProjectRecord p=getItem(pos); ((TextView)convert.findViewById(R.id.itemTitle)).setText(p.projectName); ((TextView)convert.findViewById(R.id.itemSubtitle)).setText("العميل: "+p.clientName+"  •  "+p.totalBoxes+" علبة"); ((TextView)convert.findViewById(R.id.itemMeta)).setText("تكلفة العلبة: "+FormatUtils.money(p.unitCost)+" ريال  |  سعر البيع المقترح: "+FormatUtils.money(sell(p))+" ريال"); return convert; }}; list.setAdapter(adapter); list.setOnItemClickListener((pa,v,pos,id)->showActions(items.get(pos))); }
 @Override protected void onResume(){ super.onResume(); reload(); }
 private void reload(){ items.clear(); items.addAll(db.getProjects()); adapter.notifyDataSetChanged(); empty.setVisibility(items.isEmpty()?View.VISIBLE:View.GONE); }
 private JSONObject state(ProjectRecord p){ try{return new JSONObject(p.stateJson==null||p.stateJson.trim().isEmpty()?"{}":p.stateJson);}catch(Exception e){return new JSONObject();} }
 private double sell(ProjectRecord p){ try{JSONObject r=state(p).optJSONObject("result"); if(r!=null&&r.has("sellPrice")) return r.optDouble("sellPrice",p.unitCost);}catch(Exception ignored){} if(p.totalBoxes>0&&p.profitValue>0)return p.unitCost+p.profitValue/p.totalBoxes; return p.unitCost*(1+Math.max(0,p.profitPercent)/100.0); }
 private String side(boolean i,boolean o){return i&&o?"داخلي وخارجي":i?"داخلي":o?"خارجي":"بدون";}
 private void showActions(ProjectRecord p){ String details="العميل: "+p.clientName+"\nالتاريخ: "+p.createdAt+"\nوزن الورق: "+p.gsm+" جم\nالكمية: "+p.totalBoxes+" علبة\nالشيتات: "+p.sheetsCount+"\nالإجمالي: "+FormatUtils.money(p.totalCost)+" ريال\nسعر العلبة: "+FormatUtils.money(p.unitCost)+" ريال\nسعر البيع المقترح: "+FormatUtils.money(sell(p))+" ريال/علبة"+(p.notes.isEmpty()?"":"\n\nملاحظات: "+p.notes); new AlertDialog.Builder(this).setTitle(p.projectName).setMessage(details).setPositiveButton("تحميل للحاسبة",(d,w)->{Intent i=new Intent(this,MainActivity.class);i.putExtra("load_project_id",p.id);i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);startActivity(i);finish();}).setNeutralButton("مشاركة",(d,w)->share(p)).setNegativeButton("حذف",(d,w)->confirmDelete(p)).show(); }
 private void confirmDelete(ProjectRecord p){new AlertDialog.Builder(this).setTitle("حذف المشروع").setMessage("هل تريد حذف «"+p.projectName+"»؟").setPositiveButton("حذف",(d,w)->{db.deleteProject(p.id);reload();Toast.makeText(this,"تم الحذف",Toast.LENGTH_SHORT).show();}).setNegativeButton("إلغاء",null).show();}
 private void share(ProjectRecord p){JSONObject st=state(p),r=st.optJSONObject("result");String size=st.optString("size","70 × 100");int count=st.optInt("count",p.totalBoxes),per=st.optInt("perSheet",p.boxesPerSheet),gram=st.optInt("grammage",p.gsm),sheets=r==null?p.sheetsCount:(int)Math.ceil(r.optDouble("sheets",p.sheetsCount)),glue=st.optInt("lzPoints",p.gluePoints);String t="إبداعات دوما — "+p.projectName+"\nالعميل: "+p.clientName+"\nالمقاس: "+size+" سم\nالجراماج: "+gram+" جرام\nالكمية: "+count+" علبة\nعدد العلب في الشيت: "+per+"\nعدد الشيتات: "+sheets+" شيت\nالطباعة: "+side(st.optBoolean("innerPrint",false),st.optBoolean("outerPrint",false))+"\nالسولفان: "+side(st.optBoolean("innerSilver",p.laminationInside),st.optBoolean("outerSilver",p.laminationOutside))+"\nالورنيش: "+side(st.optBoolean("innerVarnish",p.varnishInside),st.optBoolean("outerVarnish",p.varnishOutside))+"\nنقاط التلزيق: "+(glue>0?glue+" نقطة":"بدون")+"\nالقالب: "+(st.optBoolean("newMold",p.newDie)?"قالب جديد":"قالب متوفر / بدون قالب جديد")+"\nأدنى سعر للبيع: "+FormatUtils.money(sell(p))+" ر.س للحبة";Intent s=new Intent(Intent.ACTION_SEND);s.setType("text/plain");s.putExtra(Intent.EXTRA_TEXT,t);startActivity(Intent.createChooser(s,"مشاركة المشروع"));}
}
''', encoding='utf-8')

build_path = root / 'app/build.gradle.kts'
build = build_path.read_text(encoding='utf-8')
build = re.sub(r'versionCode\s*=\s*\d+', 'versionCode = 4', build)
build = re.sub(r'versionName\s*=\s*"[^"]+"', 'versionName = "1.1.2"', build)
build_path.write_text(build, encoding='utf-8')
print('Patched Ibdaat Duma v1.1.2')
