#!/usr/bin/env python3
"""Make transparent clock preview and day/night Qasioun prayer style QA images."""
import sys
from pathlib import Path
from PIL import Image,ImageDraw,ImageFont

root=Path(sys.argv[1]).resolve()
font_dir=root/"app/src/main/res/font"
out=root/"app/src/main/res/drawable-nodpi"
out.mkdir(parents=True,exist_ok=True)
sans="/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"

def font(size,bold=False):
    return ImageFont.truetype(str(font_dir/("noto_kufi_arabic_bold.ttf" if bold else
                                            "noto_kufi_arabic_regular.ttf")),size)
def nums(size):
    return ImageFont.truetype(sans,size)
def ar(draw,x,y,text,size,color,bold=False,anchor="rt"):
    draw.text((x,y),text,font=font(size,bold),fill=color,anchor=anchor,direction="rtl")
def en(draw,x,y,text,size,color,anchor="lt"):
    draw.text((x,y),text,font=nums(size),fill=color,anchor=anchor)

def clock(day,path):
    W,H=420,630
    im=Image.new("RGBA",(W,H),(0,0,0,0)) # deliberately no painted background
    d=ImageDraw.Draw(im)
    primary="#0B352D" if day else "#F7F1E4"
    muted="#31584D" if day else "#D8E4DD"
    accent="#916324" if day else "#E6C98E"
    ar(d,W//2,35,"جدة",29,accent,True,"mt")
    en(d,W//2,153,"9:06" if day else "7:15",79,primary,"mm")
    d.line((55,217,W-55,217),fill=accent,width=2)
    ar(d,W//2,242,"السبت ١٠ أكتوبر ٢٠٢٦",21,primary,False,"mt")
    ar(d,W//2,290,"٢٩ ربيع الثاني ١٤٤٨هـ",20,accent,False,"mt")
    d.line((55,340,W-55,340),fill=accent,width=2)
    # Vector weather emblem avoids missing symbol glyphs in the HONOR picker.
    cx,cy=W//2,403
    import math
    if day:
        d.ellipse((cx-24,cy-24,cx+24,cy+24),fill=accent)
        for a in range(0,360,45):
            angle=math.radians(a)
            d.line((cx+34*math.cos(angle),cy+34*math.sin(angle),
                    cx+48*math.cos(angle),cy+48*math.sin(angle)),fill=accent,width=4)
    else:
        d.ellipse((cx-33,cy-33,cx+33,cy+33),fill=accent)
        d.ellipse((cx-16,cy-45,cx+46,cy+17),fill=(0,0,0,0))
    en(d,W//2,450,"31°" if day else "24°",67,primary,"mm")
    ar(d,W//2,524,"صحو" if day else "سماء صافية",24,primary,False,"mt")
    ar(d,W//2,575,"طقس حسب الموقع",17,muted,False,"mt")
    im.save(path,optimize=True)
    # All outermost pixels must remain alpha zero.
    assert im.getpixel((0,0))[3]==0 and im.getpixel((W-1,H-1))[3]==0
    return im

def prayer(day,expanded,path):
    W,H=940,(435 if expanded else 229)
    bg=("#EAF9F3E7" if day else "#E80C4035") # indicative opaque RGB sample
    base="#FAF3E7" if day else "#0D4439"
    border="#A78B54" if day else "#D6BD81"
    primary="#0A352D" if day else "#F4F0E7"
    accent="#916324" if day else "#E6C98E"
    muted="#48645A" if day else "#C4D5CD"
    im=Image.new("RGB",(W,H),"#90AA9A")
    d=ImageDraw.Draw(im)
    d.rounded_rectangle((4,4,W-4,H-4),radius=50,fill=base,outline=border,width=3)
    ar(d,W-45,20,"جدة",35,accent,True)
    ar(d,60,22,"29 ربيع الثاني 1448 هـ",22,muted,False,"lt")
    d.line((33,82,W-33,82),fill=border,width=2)
    if expanded:
        d.rounded_rectangle((32,100,W-32,231),radius=25,
                            fill="#F2E8D6" if day else "#195245")
        ar(d,W-57,126,"العصر",38,primary,True)
        en(d,W-159,184,"03:33",27,muted)
        ar(d,60,125,"المتبقي للأذان",22,muted,False,"lt")
        en(d,60,173,"1:39:18",43,accent)
        prayer_names=["الفجر","الشروق","الظهر","العصر","المغرب","العشاء"]
        times=["05:01","06:17","12:10","03:33","06:04","07:34"]
        for i,(name,time) in enumerate(zip(prayer_names,times)):
            row,col=divmod(i,3)
            x=W-155-col*310
            y=250+row*81
            ar(d,x,y,name,25,accent if i==1 else primary,False,"mt")
            en(d,x,y+41,time,26,accent if i==1 else muted,"mt")
    else:
        ar(d,W-45,105,"العصر",38,primary,True)
        en(d,W-165,162,"03:33",27,muted)
        ar(d,60,108,"المتبقي للأذان",22,muted,False,"lt")
        en(d,60,159,"1:39:18",42,accent)
    im.save(path,optimize=True)
    return im

dayClock=clock(True,out/"clock_weather_preview.png")
nightClock=clock(False,out/"clock_weather_preview_night.png")
day=prayer(True,False,out/"widget_preview_compact_day.png")
night=prayer(False,False,out/"widget_preview_compact_night.png")
dayLong=prayer(True,True,out/"widget_preview_expanded_day.png")
nightLong=prayer(False,True,out/"widget_preview_expanded_night.png")
# set drawer previews to a daytime static image; actual widget layout changes at sunrise/sunset.
xml_dir=root/"app/src/main/res/xml"
for name,image in [("prayer_widget_compact.xml","widget_preview_compact_day"),
                   ("prayer_widget.xml","widget_preview_expanded_day")]:
    p=xml_dir/name
    x=p.read_text()
    old='android:previewImage="@drawable/widget_preview_compact"' if "compact" in name else \
        'android:previewImage="@drawable/widget_preview_expanded"'
    new=f'android:previewImage="@drawable/{image}"'
    assert x.count(old)==1
    p.write_text(x.replace(old,new),encoding="utf-8")

# Visual QA contact sheet comparing styles, not a screenshot.
gallery=Image.new("RGB",(1280,1060),"#E5E3D8")
d=ImageDraw.Draw(gallery)
d.text((35,15),"QA — actual v09 palette assets (not a device screenshot)",fill="#092D26",font=nums(29))
for left,image,title in [(35,day,"DAY / ivory"),(35,night,"NIGHT / green")]:
    pass
from PIL import ImageOps
thumbs=[
 (day,"PRAYER DAY 4x1",(30,75)),
 (night,"PRAYER NIGHT 4x1",(30,365)),
 (dayClock,"CLOCK TRANSPARENT DAY",(790,74)),
 (nightClock,"CLOCK TRANSPARENT NIGHT",(790,574))
]
for image,label,pos in thumbs:
    ox,oy=pos
    if image.width>580:
        image=image.resize((580,round(image.height*580/image.width)))
    elif image.width>390:
        image=image.resize((360,round(image.height*360/image.width)))
    if image.mode=="RGBA":
        panel=Image.new("RGB",image.size,"#A6CBDD" if "DAY" in label else "#102A3D")
        panel.paste(image,(0,0),image)
        gallery.paste(panel,(ox,oy+45))
    else:
        gallery.paste(image,(ox,oy+45))
    d.text((ox,oy+8),label,fill="#12372D",font=nums(22))
gallery.save(out/"day_night_widget_qa_v09.png",optimize=True)
print("PASS: transparent RGBA clock with zero-alpha corners; daylight/night widget preview assets generated")
