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
 * HONOR HNT is NOT applied by this app. Only background poster + fully rendered
 * 8-second video from R16 are extracted with user-granted Storage Access Framework.
 * This prevents broken flag polygons and makes live wallpaper playback deterministic.
 */
public final class ThemeStorage {
    private static final String PREFERENCES="qasioun_theme_files_v14";
    private ThemeStorage(){}

    private static SharedPreferences pref(Context c){
        return c.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE);
    }
    public static File image(Context c,boolean night){
        return new File(c.getFilesDir(),night?"qasioun_night.jpg":"qasioun_day.jpg");
    }
    public static File video(Context c,boolean night){
        return new File(c.getFilesDir(),night?"qasioun_night.mp4":"qasioun_day.mp4");
    }
    public static boolean ready(Context c){
        return image(c,false).isFile() && video(c,false).isFile()
            && image(c,true).isFile() && video(c,true).isFile();
    }
    public static String selected(Context c,boolean night) {
        return pref(c).getString(night?"night_name":"day_name","لم يتم الاختيار");
    }
    public static void saveFolder(Context c,Uri treeUri) {
        pref(c).edit().putString("tree_uri",treeUri.toString()).apply();
    }
    public static Uri folder(Context c) {
        String text=pref(c).getString("tree_uri",null);
        try {return text==null?null:Uri.parse(text);}
        catch(Exception invalid){return null;}
    }

    private static final class Candidate {
        final Uri uri;
        final String name;
        final int priority;
        Candidate(Uri u,String n,int p){uri=u;name=n;priority=p;}
    }
    private static int priority(String fileName,boolean night){
        String n=fileName.toLowerCase(Locale.ROOT);
        if(!n.endsWith(".hnt") || !n.startsWith("syria_qasioun_"))return -1;
        if(!n.contains(night?"_night_":"_day_"))return -1;
        if(n.contains("_r16_"))return 16;
        if(n.contains("_r15_"))return 15;
        if(n.contains("_r14_"))return 14;
        if(n.contains("_r13_"))return 13;
        return 2;
    }

    public static String importFolder(Context c,Uri folderUri) throws Exception {
        if(folderUri==null)throw new IOException("حدد مجلد Honor/Themes");
        String folderId=DocumentsContract.getTreeDocumentId(folderUri);
        Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(folderUri,folderId);
        String[] columns={DocumentsContract.Document.COLUMN_DOCUMENT_ID,
            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE};
        Candidate day=null,night=null;
        int count=0;
        try(Cursor cur=c.getContentResolver().query(children,columns,null,null,null)){
            if(cur==null)throw new IOException("رفض مدير الملفات قراءة المجلد");
            int id=cur.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            int name=cur.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            int mime=cur.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE);
            while(cur.moveToNext()){
                if(++count>1800)throw new IOException("ملفات كثيرة؛ اختر كل HNT منفردًا");
                if(DocumentsContract.Document.MIME_TYPE_DIR.equals(cur.getString(mime)))continue;
                String filename=cur.getString(name);
                if(filename==null)continue;
                int d=priority(filename,false),n=priority(filename,true);
                if(d<0&&n<0)continue;
                Uri u=DocumentsContract.buildDocumentUriUsingTree(folderUri,cur.getString(id));
                if(d>=0&&(day==null||d>day.priority))day=new Candidate(u,filename,d);
                if(n>=0&&(night==null||n>night.priority))night=new Candidate(u,filename,n);
            }
        }
        if(day==null||night==null)
            throw new IOException("لم أجد ملفي R16 النهاري والليلي؛ اخترهما مباشرة من مدير الملفات");
        // Never silently fall back to older R13 images without video.
        if(day.priority<16||night.priority<16)
            throw new IOException("ملف R16 غير موجود؛ أضف نسختي R16 ذات العلم المصحح");
        importHnt(c,day.uri,false,day.name);
        importHnt(c,night.uri,true,night.name);
        saveFolder(c,folderUri);
        return "تم تحميل فيديو النهار والليل من R16 بنجاح";
    }
    private static int extract(ZipInputStream zip,File file,int max) throws IOException {
        int bytes=0;
        try(FileOutputStream output=new FileOutputStream(file)){
            byte[] buffer=new byte[16384];
            int n;
            while((n=zip.read(buffer))!=-1){
                bytes+=n;
                if(bytes>max)throw new IOException("الملف المضغوط أكبر من الحد المتوقع");
                output.write(buffer,0,n);
            }
            output.flush();
        }
        return bytes;
    }
    private static void validateVideo(File file) throws IOException {
        if(file.length()<150_000||file.length()>28_000_000)throw new IOException("حجم فيديو العلم غير مناسب");
        MediaMetadataRetriever probe=new MediaMetadataRetriever();
        try{
            probe.setDataSource(file.getAbsolutePath());
            String w=probe.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
            String h=probe.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
            String duration=probe.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if(w==null||h==null||duration==null)
                throw new IOException("تعذر قراءة خصائص الفيديو");
            int width=Integer.parseInt(w),height=Integer.parseInt(h);
            long ms=Long.parseLong(duration);
            if(width<800||height<1600||height<=1.5*width||ms<3500||ms>14000)
                throw new IOException("فيديو القفل ليس عموديًا أو مدته غير صحيحة");
        }catch(RuntimeException unsupported){
            throw new IOException("فيديو MP4 غير قابل للفك على هذا الهاتف",unsupported);
        }finally{
            probe.release();
        }
    }
    public static int importHnt(Context c,Uri source,boolean night,String fileName) throws IOException {
        if(source==null)throw new IOException("لم يتم تحديد ملف HNT");
        File tmpImage=new File(c.getFilesDir(),night?"tmp_night_poster.jpg":"tmp_day_poster.jpg");
        File tmpVideo=new File(c.getFilesDir(),night?"tmp_night_video.mp4":"tmp_day_video.mp4");
        File destImage=image(c,night),destVideo=video(c,night);
        tmpImage.delete();tmpVideo.delete();
        boolean imageFound=false,videoFound=false;
        int jpgBytes=0,videoBytes=0;
        try(InputStream raw=c.getContentResolver().openInputStream(source)){
            if(raw==null)throw new IOException("لا يمكن فتح "+fileName);
            try(ZipInputStream archive=new ZipInputStream(new BufferedInputStream(raw))) {
                ZipEntry e;
                int count=0;
                while((e=archive.getNextEntry())!=null) {
                    if(++count>4500)throw new IOException("عدد ملفات HNT غير متوقع");
                    if(e.isDirectory())continue;
                    if("wallpaper/unlock_wallpaper_0.jpg".equals(e.getName())){
                        jpgBytes=extract(archive,tmpImage,12_000_000);
                        imageFound=true;
                    }else if("unlock/lockscreen/unlock.mp4".equals(e.getName())){
                        videoBytes=extract(archive,tmpVideo,28_000_000);
                        videoFound=true;
                    }
                    if(imageFound&&videoFound)break;
                    archive.closeEntry();
                }
            }
            if(!imageFound||jpgBytes<40_000)
                throw new IOException("خلفية القفل العمودية غير موجودة في "+fileName);
            if(!videoFound||videoBytes<150_000)
                throw new IOException("فيديو R16 غير موجود داخل HNT؛ اختر نسخة R16");
            BitmapFactory.Options o=new BitmapFactory.Options();
            o.inJustDecodeBounds=true;
            BitmapFactory.decodeFile(tmpImage.getAbsolutePath(),o);
            if(o.outWidth<800||o.outHeight<1600||o.outHeight<=1.5*o.outWidth)
                throw new IOException("صورة قفل غير مناسبة");
            validateVideo(tmpVideo);
            File oldImage=new File(c.getFilesDir(),night?"old_night_image":"old_day_image");
            File oldVideo=new File(c.getFilesDir(),night?"old_night_video":"old_day_video");
            oldImage.delete();oldVideo.delete();
            boolean posterMoved=!destImage.exists()||destImage.renameTo(oldImage);
            boolean videoMoved=!destVideo.exists()||destVideo.renameTo(oldVideo);
            if(!posterMoved||!videoMoved){
                if(oldImage.exists())oldImage.renameTo(destImage);
                if(oldVideo.exists())oldVideo.renameTo(destVideo);
                throw new IOException("تعذّر تحديث المحتوى المخزن");
            }
            if(!tmpImage.renameTo(destImage)||!tmpVideo.renameTo(destVideo)){
                destImage.delete();destVideo.delete();
                if(oldImage.exists())oldImage.renameTo(destImage);
                if(oldVideo.exists())oldVideo.renameTo(destVideo);
                throw new IOException("فشل حفظ ملفات الخلفية");
            }
            oldImage.delete();oldVideo.delete();
            pref(c).edit().putString(night?"night_name":"day_name",fileName).apply();
            return jpgBytes+videoBytes;
        }finally {
            tmpImage.delete();
            tmpVideo.delete();
        }
    }
}
