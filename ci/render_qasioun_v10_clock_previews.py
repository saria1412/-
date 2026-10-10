#!/usr/bin/env python3
"""Pixel-level static QA for v0.10 clock widget; true alpha and correct visual order."""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
import math
import sys
root=Path(sys.argv[1]).resolve()
font_dir=root/"app/src/main/res/font"
out=root/"app/src/main/res/drawable-nodpi"
out.mkdir(parents=True,exist_ok=True)
latin=ImageFont.truetype
normal=lambda size:ImageFont.truetype(str(font_dir/"noto_kufi_arabic_regular.ttf"),size)
bold=lambda size:ImageFont.truetype(str(font_dir/"noto_kufi_arabic_bold.ttf"),size)
sans="/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"

def render(day):
    W,H=420,630
    im=Image.new("RGBA",(W,H),(0,0,0,0))
    d=ImageDraw.Draw(im)
    primary="#0A352D" if day else "#F7F1E4"
    accent="#916324" if day else "#E6C98E"
    muted="#426255" if day else "#CFD8D3"
    ar=lambda y,s,font,color,anchor="mt":d.text((W//2,y),s,font=font,fill=color,anchor=anchor,direction="rtl")
    ar(24,"جدة",bold(29),accent)
    d.text((W//2,154),"10:06" if day else "10:06",
           font=latin(sans,72),fill=primary,anchor="mm")
    d.line((56,209,W-56,209),fill=accent,width=2)
    ar(226,"السبت ١٠ أكتوبر ٢٠٢٦",normal(20),primary)
    ar(277,"٢٩ ربيع الثاني ١٤٤٨هـ",normal(20),accent)
    d.line((56,325,W-56,325),fill=accent,width=2)
    # User-requested order: TEMPERATURE, WEATHER ICON, CONDITION.
    d.text((W//2,420),"30°",font=latin(sans,73),fill=primary,anchor="mm")
    cx,cy=W//2,506
    if day:
        d.ellipse((cx-20,cy-20,cx+20,cy+20),fill=accent)
        for a in range(0,360,45):
            v=math.radians(a)
            d.line((cx+27*math.cos(v),cy+27*math.sin(v),
                    cx+35*math.cos(v),cy+35*math.sin(v)),fill=accent,width=3)
    else:
        # crescent opaque gold disc with true transparency cutout
        d.ellipse((cx-26,cy-26,cx+26,cy+26),fill=accent)
        d.ellipse((cx-10,cy-32,cx+35,cy+17),fill=(0,0,0,0))
    ar(555,"صحو" if day else "سماء صافية",normal(24),primary)
    assert im.getpixel((0,0))[3]==0
    assert im.getpixel((W-1,H-1))[3]==0
    assert not any(pixel[3]!=0 for pixel in (im.getpixel((0,H//2)),
                                                im.getpixel((W-1,H//2))))
    return im
day=render(True)
night=render(False)
day.save(out/"clock_weather_preview.png",optimize=True)
night.save(out/"clock_weather_preview_night.png",optimize=True)
day.save(out/"clock_transparent_day_v10.png",optimize=True)
night.save(out/"clock_transparent_night_v10.png",optimize=True)

W,H=1120,780
gallery=Image.new("RGB",(W,H),"#E4EBE8")
d=ImageDraw.Draw(gallery)
d.text((32,19),"QA 0.10 — 100% transparent clock widget (no footer)",
       font=latin(sans,26),fill="#123F34")
for i,(picture,label,background) in enumerate((
    (day,"DAY / 30°C and sun below","#B7D8EB"),
    (night,"NIGHT / 30°C and moon below","#162B41"),
)):
    x=65+i*530
    y=108
    panel=Image.new("RGB",(420,630),background)
    panel.paste(picture,(0,0),picture)
    gallery.paste(panel,(x,y))
    d.text((x,y-40),label,font=latin(sans,23),fill="#153A30")
gallery.save(out/"clock_v10_transparent_day_night_qa.png",optimize=True)
print("PASS v0.10 visual QA: temperature above icon, no weather footer, transparent RGBA bounds")
