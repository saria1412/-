#!/usr/bin/env python3
"""Generate actual widget-picker thumbnail for the tall Qasioun clock/weather widget."""
from PIL import Image,ImageDraw,ImageFont
from pathlib import Path
import sys,math

root=Path(sys.argv[1]).resolve()
fonts=root/"app/src/main/res/font"
out=root/"app/src/main/res/drawable-nodpi/clock_weather_preview.png"
out.parent.mkdir(parents=True,exist_ok=True)

W,H=400,600
im=Image.new("RGBA",(W,H),(0,0,0,0))
d=ImageDraw.Draw(im)
d.rounded_rectangle((6,6,W-6,H-6),radius=49,fill=(7,52,43,224),outline=(230,201,142,190),width=4)
def arabic(sz,bold=False):
    name="noto_kufi_arabic_bold.ttf" if bold else "noto_kufi_arabic_regular.ttf"
    return ImageFont.truetype(str(fonts/name),sz)
latin=lambda sz:ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",sz)
gold=(230,201,142,255)
cream=(244,241,224,255)
pale=(217,223,216,255)
d.text((W//2,25),"جدة",font=arabic(25,True),fill=gold,anchor="mt",direction="rtl")
d.text((W//2,103),"9:06",font=latin(74),fill=cream,anchor="mm")
d.line((39,162,W-39,162),fill=(212,188,123,160),width=2)
d.text((W//2,184),"السبت ١٠ أكتوبر ٢٠٢٦",font=arabic(17),fill=cream,anchor="mt",direction="rtl")
d.text((W//2,230),"٢٩ ربيع الآخر ١٤٤٨هـ",font=arabic(17),fill=gold,anchor="mt",direction="rtl")
d.line((39,288,W-39,288),fill=(212,188,123,160),width=2)
# Gold sun icon drawn with vector-like strokes.
cx,cy=200,362
d.ellipse((cx-24,cy-24,cx+24,cy+24),fill=gold)
for a in range(0,360,45):
    angle=math.radians(a)
    d.line((cx+31*math.cos(angle),cy+31*math.sin(angle),
            cx+49*math.cos(angle),cy+49*math.sin(angle)),
           fill=gold,width=5)
d.text((W//2,459),"31°",font=latin(61),fill=cream,anchor="mm")
d.text((W//2,518),"صحو",font=arabic(23),fill=cream,anchor="mm",direction="rtl")
d.text((W//2,567),"طقس حسب الموقع",font=arabic(16),fill=pale,anchor="mm",direction="rtl")
im.save(out,optimize=True)
assert out.stat().st_size>12000
print("Preview created:",out.name,im.size,out.stat().st_size)
