package com.sarf.qasioun.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.DocumentsContract;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Sandboxed SAF import. R16 video is extracted from the user's own day/night HNT
 * and played by hardware decoding; the app never changes the system theme.
 */
public final class ThemeStorage {
    private static final String PREFERENCES="qasioun_theme_files_v2";
    private ThemeStorage() {}
    private static SharedPreferences pref(Context c) {
        return c.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE);
    }
    public static File image(Context c,boolean night) {
        return new File(c.getFilesDir(),night?"qasioun_night.jpg":"qasioun_day.jpg");
    }
    public static File video(Context c,boolean night) {
        return new File(c.getFilesDir(),night?"qasioun_night_video.mp4":"qasioun_day_video.mp4");
    }
    private static boolean isPortrait(File file) {
        if(!file.isFile())return false;
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(file.getAbsolutePath(),options);
        return options.outWidth>=800 && options.outHeight>=1600
            && options.outHeight>1.65*options.outWidth;
    }
    public static boolean ready(Context c) {
        return isPortrait(image(c,false)) && isPortrait(image(c,true));
    }
    public static boolean hasMotion(Context c,boolean night) {
        return video(c,night).isFile() && video(c,night).length()>80_000;
    }
    public static String selected(Context c,boolean night) {
        return pref(c).getString(night?"night_name":"day_name","لم يتم الاختيار");
    }
    public static void saveFolder(Context c,Uri uri) {
        pref(c).edit().putString("tree_uri",uri.toString()).apply();
    }
    public static Uri folder(Context c) {
        String name=pref(c).getString("tree_uri",null);
        try {return name==null?null:Uri.parse(name);}
        catch(RuntimeException invalid){return null;}
    }
    private static final class Candidate {
        final Uri uri;
        final String name;
        final int rank;
        Candidate(Uri uri,String name,int rank) {
            this.uri=uri;this.name=name;this.rank=rank;
        }
    }
    private static int rank(String filename,boolean night) {
        String n=filename.toLowerCase(Locale.ROOT);
        if(!n.endsWith(".hnt") || !n.startsWith("syria_qasioun_"))return -1;
        if(!n.contains(night?"_night_":"_day_"))return -1;
        if(n.contains("_r16_"))return 16;
        if(n.contains("_r15_"))return 15;
        if(n.contains("_r14_"))return 14;
        if(n.contains("_r13_"))return 13;
        return 2;
    }
    public static String importFolder(Context c,Uri folderUri) throws Exception {
        if(folderUri==null)throw new IOException("اختر مجلد Honor/Themes");
        String folderId=DocumentsContract.getTreeDocumentId(folderUri);
        Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(folderUri,folderId);
        String[] columns={
            DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE
        };
        Candidate day=null,night=null;
        int checked=0;
        try(Cursor cursor=c.getContentResolver().query(children,columns,null,null,null)) {
            if(cursor==null)throw new IOException("تعذر قراءة المجلد. اختر الملفات منفردة.");
            int idIndex=cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            int nameIndex=cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            int mimeIndex=cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE);
            while(cursor.moveToNext()) {
                if(++checked>2000)throw new IOException("المجلد كبير جدًا؛ اختر الملفات منفردة.");
                if(DocumentsContract.Document.MIME_TYPE_DIR.equals(cursor.getString(mimeIndex)))continue;
                String name=cursor.getString(nameIndex);
                if(name==null)continue;
                int d=rank(name,false),n=rank(name,true);
                if(d<0&&n<0)continue;
                Uri uri=DocumentsContract.buildDocumentUriUsingTree(folderUri,cursor.getString(idIndex));
                if(d>=0&&(day==null||d>day.rank))day=new Candidate(uri,name,d);
                if(n>=0&&(night==null||n>night.rank))night=new Candidate(uri,name,n);
            }
        }
        if(day==null||night==null)
            throw new IOException("يجب وجود ملفَي Qasioun Day وNight في المجلد");
        importHnt(c,day.uri,false,day.name);
        importHnt(c,night.uri,true,night.name);
        saveFolder(c,folderUri);
        return "تم استيراد "+day.name+" و "+night.name;
    }
    private static int copyEntry(ZipInputStream zip,File file,int limit) throws IOException {
        int count=0;
        try(FileOutputStream out=new FileOutputStream(file)) {
            byte[] buffer=new byte[8192];
            int n;
            while((n=zip.read(buffer))!=-1) {
                count+=n;
                if(count>limit)throw new IOException("حجم مورد HNT غير متوقع");
                out.write(buffer,0,n);
            }
            out.flush();
        }
        return count;
    }
    private static boolean validVideo(File file) {
        if(!file.isFile()||file.length()<80_000||file.length()>20_000_000)return false;
        MediaMetadataRetriever md=new MediaMetadataRetriever();
        try {
            md.setDataSource(file.getAbsolutePath());
            String duration=md.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            String width=md.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String height=md.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            if(duration==null||width==null||height==null)return false;
            long ms=Long.parseLong(duration);
            return ms>=1000&&ms<=30_000&&Integer.parseInt(width)>=540&&Integer.parseInt(height)>=960;
        }catch(Exception invalid){return false;}
        finally{try{md.release();}catch(Exception ignored){}}
    }
    private static void replace(File source,File target) throws IOException {
        File backup=new File(target.getAbsolutePath()+".bak");
        if(backup.exists())backup.delete();
        if(target.exists()&&!target.renameTo(backup))throw new IOException("تعذر تحديث الخلفية القديمة");
        if(!source.renameTo(target)) {
            if(backup.exists())backup.renameTo(target);
            throw new IOException("تعذر حفظ المورد الجديد");
        }
        backup.delete();
    }

    public static int importHnt(Context c,Uri source,boolean night,String name) throws IOException {
        if(source==null)throw new IOException("ملف HNT غير موجود");
        File photoTemp=new File(c.getFilesDir(),night?"night_image.new":"day_image.new");
        File videoTemp=new File(c.getFilesDir(),night?"night_video.new":"day_video.new");
        photoTemp.delete();videoTemp.delete();
        int bytes=0,videoBytes=0;
        boolean foundPhoto=false,foundVideo=false;
        try {
            try(InputStream raw=c.getContentResolver().openInputStream(source)) {
                if(raw==null)throw new IOException("لا يمكن فتح ملف HNT");
                try(ZipInputStream zip=new ZipInputStream(new BufferedInputStream(raw))) {
                    ZipEntry entry;
                    int checked=0;
                    while((entry=zip.getNextEntry())!=null) {
                        if(++checked>4000)throw new IOException("ملف HNT يحتوي موارد كثيرة جدًا");
                        String key=entry.getName();
                        if(entry.isDirectory())continue;
                        if("wallpaper/unlock_wallpaper_0.jpg".equals(key)&&!foundPhoto) {
                            bytes=copyEntry(zip,photoTemp,12_000_000);
                            foundPhoto=true;
                        } else if("unlock/lockscreen/unlock.mp4".equals(key)&&!foundVideo) {
                            videoBytes=copyEntry(zip,videoTemp,20_000_000);
                            foundVideo=true;
                        }
                        zip.closeEntry();
                        if(foundPhoto&&foundVideo)break;
                    }
                }
            }
            if(!foundPhoto||bytes<20_000||!isPortrait(photoTemp))
                throw new IOException("خلفية القفل الطولية غير صالحة");
            if(foundVideo&&!validVideo(videoTemp))
                throw new IOException("فيديو قفل HNT غير صالح أو غير مدعوم");
            replace(photoTemp,image(c,night));
            if(foundVideo)replace(videoTemp,video(c,night));
            else video(c,night).delete();  // avoid playing a previous theme's video
            pref(c).edit().putString(night?"night_name":"day_name",name).apply();
            return bytes+videoBytes;
        }finally {
            photoTemp.delete();videoTemp.delete();
        }
    }
}
