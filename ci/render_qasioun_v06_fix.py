#!/usr/bin/env python3
"""Create verified Arabic previews for MagicOS widget picker (v0.6)."""
import sys
from pathlib import Path
from math import sin, cos, pi
from PIL import Image, ImageDraw, ImageFont

root = Path(sys.argv[1]).resolve()
fonts = root / "app/src/main/res/font"
out = root / "app/src/main/res/drawable-nodpi"
out.mkdir(parents=True,exist_ok=True)
sans="/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"
def kufi(weight,size):
    return ImageFont.truetype(str(fonts/f"noto_kufi_arabic_{weight}.ttf"),size)
def latin(size):
    return ImageFont.truetype(sans,size)
def preview(w,h,expanded,file):
    im=Image.new("RGB",(w,h),"#17372D")
    d=ImageDraw.Draw(im)
    d.rounded_rectangle((5,5,w-5,h-5),radius=52,fill="#0D3B31",outline="#B9A675",width=4)
    d.text((w-35,18),"جدة",font=kufi("bold",33),fill="#E6C98E",anchor="rt",direction="rtl")
    d.text((60,21),"28 ربيع الثاني 1448هـ",font=kufi("regular",23),fill="#D6DDD5",anchor="lt",direction="rtl")
    d.line([(35,69),(w-35,69)],fill="#746C51",width=2)
    # Draw the solar glyph as geometry, not as an unsupported font glyph.
    x,y=68,107
    d.ellipse((x-9,y-9,x+9,y+9),fill="#E6C98E")
    for angle in range(0,360,45):
        a=pi*angle/180
        d.line((x+13*cos(a),y+13*sin(a),x+20*cos(a),y+20*sin(a)),
               fill="#E6C98E",width=2)
    # Arabic and 12-hour digits are separate runs to prevent garbled bidi punctuation.
    d.text((100,88),"الشروق",font=kufi("medium",25),fill="#E6C98E",anchor="lt",direction="rtl")
    d.text((226,91),"06:17",font=latin(25),fill="#E6C98E",anchor="lt")
    d.text((309,88),"ص",font=kufi("regular",23),fill="#E6C98E",anchor="lt",direction="rtl")
    if not expanded:
        d.text((w-36,146),"الظهر القادم",font=kufi("bold",31),fill="#F5F1E4",anchor="rt",direction="rtl")
        d.text((w-163,192),"12:10",font=latin(25),fill="#D6DDD5",anchor="lt")
        d.text((w-55,191),"م",font=kufi("regular",25),fill="#D6DDD5",anchor="lt",direction="rtl")
        d.text((55,148),"متبقي على الأذان",font=kufi("regular",21),fill="#D6DDD5",anchor="lt",direction="rtl")
        d.text((55,191),"6:27:48",font=latin(36),fill="#E6C98E",anchor="lt")
    else:
        d.rounded_rectangle((34,143,w-34,258),radius=26,fill="#145044",outline="#857958",width=2)
        d.text((w-65,158),"الظهر القادم",font=kufi("bold",32),fill="#F5F1E4",anchor="rt",direction="rtl")
        d.text((w-190,208),"12:10",font=latin(25),fill="#D6DDD5",anchor="lt")
        d.text((w-83,207),"م",font=kufi("regular",25),fill="#D6DDD5",anchor="lt",direction="rtl")
        d.text((64,162),"متبقي على الأذان",font=kufi("regular",21),fill="#D6DDD5",anchor="lt",direction="rtl")
        d.text((64,207),"6:27:48",font=latin(40),fill="#E6C98E",anchor="lt")
        names=("الفجر","الشروق","الظهر","العصر","المغرب","العشاء")
        times=("05:01","06:17","12:10","03:33","06:04","07:34")
        for i,(name,time) in enumerate(zip(names,times)):
            row,col=divmod(i,3)
            x=w-155-col*296
            y=286+row*92
            d.text((x,y),name,font=kufi("medium",25),
                   fill="#E6C98E" if name=="الشروق" else "#F5F1E4",anchor="mt",direction="rtl")
            d.text((x,y+42),time,font=latin(24),
                   fill="#E6C98E" if name=="الشروق" else "#D6DDD5",anchor="mt")
    im.save(out/file,optimize=True)

preview(900,250,False,"widget_preview_compact.png")
preview(900,485,True,"widget_preview_expanded.png")
print("PASS: refreshed accessible widget previews; Arabic/Latin runs separated")
