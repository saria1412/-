#!/usr/bin/env python3
"""Android widget-picker previews for next-event-based v0.7: no persistent sunrise strip."""
import sys
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

root=Path(sys.argv[1]).resolve()
fonts=root/"app/src/main/res/font"
dest=root/"app/src/main/res/drawable-nodpi"
dest.mkdir(parents=True,exist_ok=True)
typeface=lambda kind,size:ImageFont.truetype(str(fonts/f"noto_kufi_arabic_{kind}.ttf"),size)
numbers=lambda size:ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",size)
GOLD="#E6C98E"
CREAM="#F7F1DE"
MUTED="#D7E0D8"

def ar(d,x,y,s,sz=24,bold=False,color=CREAM,align="rt"):
    d.text((x,y),s,fill=color,font=typeface("bold" if bold else "regular",sz),anchor=align,direction="rtl")
def en(d,x,y,s,sz=26,color=CREAM,anchor="lt"):
    d.text((x,y),s,font=numbers(sz),fill=color,anchor=anchor)

def widget(w,h,compact,path):
    img=Image.new("RGB",(w,h),"#173D32")
    d=ImageDraw.Draw(img)
    d.rounded_rectangle((3,3,w-3,h-3),radius=49,fill="#0B392F",outline="#BAA77A",width=4)
    ar(d,w-28,22,"جدة",29,True,GOLD)
    ar(d,48,26,"29 ربيع الثاني 1448 هـ",21,color=MUTED,align="lt")
    d.line((30,73,w-30,73),fill="#A99667",width=2)
    if compact:
        # Example after sunrise: only "الظهر" is shown, not a fixed sunrise row.
        ar(d,w-32,98,"الظهر",34,True)
        ar(d,w-32,159,"م",22,color=MUTED)
        en(d,w-152,157,"12:10",26,MUTED)
        ar(d,50,100,"المتبقي للأذان",20,color=MUTED,align="lt")
        en(d,50,157,"3:27:01",37,GOLD)
    else:
        # Example between Fajr and sunrise: dynamically "الشروق" replaces the prayer name.
        d.rounded_rectangle((27,91,w-27,229),radius=23,fill="#174A3D",outline="#867958",width=2)
        ar(d,w-50,114,"الشروق",35,True,GOLD)
        ar(d,w-50,179,"ص",22,color=MUTED)
        en(d,w-161,177,"06:17",26,MUTED)
        ar(d,53,112,"المتبقي للشروق",22,color=MUTED,align="lt")
        en(d,53,173,"0:35:00",39,GOLD)
        # Day's timetable is still listed in expanded mode, as requested previously.
        names=["الفجر","الشروق","الظهر","العصر","المغرب","العشاء"]
        times=["05:01","06:17","12:10","03:33","06:04","07:34"]
        for i,(label,value) in enumerate(zip(names,times)):
            row,col=divmod(i,3)
            x=w-160-col*290
            y=263+row*88
            ar(d,x,y,label,23,color=GOLD if label=="الشروق" else CREAM,align="mt")
            en(d,x,y+37,value,24,GOLD if label=="الشروق" else MUTED,anchor="mt")
    img.save(path,optimize=True)
    print(path.name,img.size,path.stat().st_size)

widget(900,238,True,dest/"widget_preview_compact.png")
widget(900,457,False,dest/"widget_preview_expanded.png")
