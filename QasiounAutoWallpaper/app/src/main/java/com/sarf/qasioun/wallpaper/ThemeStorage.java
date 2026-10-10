package com.sarf.qasioun.wallpaper;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** Imports home wallpaper assets only from two selected HONOR HNT archives. */
public final class ThemeStorage {
    private ThemeStorage(){}
    public static File image(Context c,boolean night){
        return new File(c.getFilesDir(), night?"qasioun_night.jpg":"qasioun_day.jpg");
    }
    public static boolean ready(Context c){
        return image(c,false).isFile() && image(c,true).isFile();
    }
    public static int importHnt(Context c,Uri file,boolean night)throws IOException {
        if(file==null)throw new IOException("ملف غير موجود");
        File temporary=new File(c.getFilesDir(),night?"night_import.tmp":"day_import.tmp");
        File image=image(c,night);
        int length=0;
        boolean found=false;
        try(InputStream raw=c.getContentResolver().openInputStream(file)){
            if(raw==null)throw new IOException("لا يمكن قراءة الملف");
            try(ZipInputStream zip=new ZipInputStream(new BufferedInputStream(raw))){
                ZipEntry entry;
                int checked=0;
                while((entry=zip.getNextEntry())!=null) {
                    if(++checked>3000)throw new IOException("الملف يحتوي موارد أكثر من المتوقع");
                    if(!entry.isDirectory() && "wallpaper/home_wallpaper_0.jpg".equals(entry.getName())){
                        try(FileOutputStream out=new FileOutputStream(temporary)){
                            byte[] bytes=new byte[8192];
                            int n;
                            while((n=zip.read(bytes))!=-1){
                                length+=n;
                                if(length>12_000_000)throw new IOException("حجم الصورة كبير جدًا");
                                out.write(bytes,0,n);
                            }
                            out.flush();
                        }
                        found=true;
                        break;
                    }
                }
            }
        }finally{
            if(!found)temporary.delete();
        }
        if(!found||length<20_000)throw new IOException("الحزمة لا تحتوي خلفية رئيسية مدعومة");
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(temporary.getAbsolutePath(),options);
        if(options.outWidth<450||options.outHeight<900||
             options.outHeight>9000||options.outWidth>9000) {
            temporary.delete();
            throw new IOException("أبعاد الصورة غير مناسبة");
        }
        File backup=new File(c.getFilesDir(),night?"night_backup.tmp":"day_backup.tmp");
        if(backup.exists())backup.delete();
        if(image.exists()&&!image.renameTo(backup)){
            temporary.delete();
            throw new IOException("تعذر حفظ الخلفية الجديدة");
        }
        if(!temporary.renameTo(image)){
            if(backup.exists())backup.renameTo(image);
            temporary.delete();
            throw new IOException("فشل حفظ الصورة");
        }
        backup.delete();
        return length;
    }
}
