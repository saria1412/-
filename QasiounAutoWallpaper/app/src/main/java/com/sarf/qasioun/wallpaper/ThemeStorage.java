package com.sarf.qasioun.wallpaper;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.BitmapFactory;
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
 * SAF-only access to /Honor/Themes. Android does not grant apps general
 * filesystem access. User grants a folder once; photos are cached privately.
 */
public final class ThemeStorage {
    private static final String PREFERENCES="qasioun_theme_files_v2";
    private ThemeStorage(){}

    private static SharedPreferences pref(Context c){
        return c.getSharedPreferences(PREFERENCES,Context.MODE_PRIVATE);
    }
    public static File image(Context c,boolean night){
        return new File(c.getFilesDir(),night?"qasioun_night.jpg":"qasioun_day.jpg");
    }
    public static boolean ready(Context c){
        return image(c,false).isFile() && image(c,true).isFile();
    }
    public static String selected(Context c,boolean night) {
        return pref(c).getString(night?"night_name":"day_name","لم يتم الاختيار");
    }
    public static void saveFolder(Context c,Uri treeUri) {
        pref(c).edit().putString("tree_uri",treeUri.toString()).apply();
    }
    public static Uri folder(Context c) {
        String uri=pref(c).getString("tree_uri",null);
        try {return uri==null?null:Uri.parse(uri);}
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
        if(n.contains("_r13_"))return 5;
        if(n.contains("_r14_"))return 6;
        return 2;
    }

    public static String importFolder(Context c,Uri folderUri) throws Exception {
        if(folderUri==null)throw new IOException("اختر مجلد Honor/Themes");
        String folderId=DocumentsContract.getTreeDocumentId(folderUri);
        Uri children=DocumentsContract.buildChildDocumentsUriUsingTree(folderUri,folderId);
        String[] columns={DocumentsContract.Document.COLUMN_DOCUMENT_ID,
          DocumentsContract.Document.COLUMN_DISPLAY_NAME,
          DocumentsContract.Document.COLUMN_MIME_TYPE};
        Candidate day=null,night=null;
        int count=0;
        try(Cursor cursor=c.getContentResolver().query(children,columns,null,null,null)){
            if(cursor==null)throw new IOException("لم يسمح مدير الملفات بقراءة المجلد");
            int idIndex=cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID);
            int nameIndex=cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME);
            int mimeIndex=cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE);
            while(cursor.moveToNext()) {
                if(++count>1500)throw new IOException("المجلد كبير جدًا؛ اختر ملفي HNT يدويًا");
                String mime=cursor.getString(mimeIndex);
                if(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime))continue;
                String name=cursor.getString(nameIndex);
                if(name==null)continue;
                int d=priority(name,false), n=priority(name,true);
                if(d<0&&n<0)continue;
                Uri u=DocumentsContract.buildDocumentUriUsingTree(folderUri,cursor.getString(idIndex));
                if(d>=0&&(day==null||d>day.priority))day=new Candidate(u,name,d);
                if(n>=0&&(night==null||n>night.priority))night=new Candidate(u,name,n);
            }
        }
        if(day==null||night==null) {
            throw new IOException("لم أجد ملفي Syria_Qasioun_Day وSyria_Qasioun_Night في المجلد المحدد");
        }
        importHnt(c,day.uri,false,day.name);
        importHnt(c,night.uri,true,night.name);
        saveFolder(c,folderUri);
        return "تم استيراد "+day.name+" و "+night.name;
    }

    /**
     * Reads only wallpaper/home_wallpaper_0.jpg from the selected HNT.
     * Hard limits mitigate accidental zip bombs and corrupted downloads.
     */
    public static int importHnt(Context c,Uri uri,boolean night,String name)throws IOException {
        if(uri==null)throw new IOException("لم يتم تحديد الملف");
        File temp=new File(c.getFilesDir(),night?"night_new.tmp":"day_new.tmp");
        File target=image(c,night);
        int length=0;
        boolean found=false;
        try(InputStream raw=c.getContentResolver().openInputStream(uri)){
            if(raw==null)throw new IOException("تعذر فتح HNT: "+name);
            try(ZipInputStream zip=new ZipInputStream(new BufferedInputStream(raw))) {
                ZipEntry entry;
                int entries=0;
                while((entry=zip.getNextEntry())!=null) {
                    if(++entries>4000)throw new IOException("حزمة تحتوي ملفات كثيرة جدًا");
                    if(!entry.isDirectory()&&"wallpaper/home_wallpaper_0.jpg".equals(entry.getName())) {
                        try(FileOutputStream out=new FileOutputStream(temp)) {
                            byte[] buffer=new byte[8192];
                            int n;
                            while((n=zip.read(buffer))!=-1) {
                                length+=n;
                                if(length>12_000_000)throw new IOException("الخلفية أكبر من الحد المسموح");
                                out.write(buffer,0,n);
                            }
                            out.flush();
                        }
                        found=true;
                        break;
                    }
                }
            }
        }finally{
            if(!found)temp.delete();
        }
        if(!found||length<20_000){
            temp.delete();
            throw new IOException("لا توجد خلفية قابلة للقراءة داخل "+name);
        }
        BitmapFactory.Options dimensions=new BitmapFactory.Options();
        dimensions.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(temp.getAbsolutePath(),dimensions);
        if(dimensions.outWidth<480||dimensions.outWidth>8000||
          dimensions.outHeight<900||dimensions.outHeight>8000){
            temp.delete();
            throw new IOException("أبعاد الخلفية غير صحيحة في "+name);
        }
        File backup=new File(c.getFilesDir(),night?"night_previous.tmp":"day_previous.tmp");
        if(backup.exists())backup.delete();
        if(target.exists()&&!target.renameTo(backup)) {
            temp.delete();
            throw new IOException("تعذر تحديث النسخة المخزنة");
        }
        if(!temp.renameTo(target)){
            if(backup.exists())backup.renameTo(target);
            temp.delete();
            throw new IOException("تعذر حفظ الخلفية");
        }
        backup.delete();
        pref(c).edit().putString(night?"night_name":"day_name",name).apply();
        return length;
    }
}
