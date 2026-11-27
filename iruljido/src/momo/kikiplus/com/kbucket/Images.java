package momo.kikiplus.com.kbucket;
import android.content.*;
import android.database.*;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import java.io.*;
public class Images extends ContentProvider {
 static String authority(Context context){return context.getPackageName()+".images";}
 static Uri fileUri(Context context,String usage,String name){
  return new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(authority(context))
      .appendPath(usage).appendPath(name).build();
 }
 public boolean onCreate(){return true;}
 private File file(Uri u){
  String n=u.getLastPathSegment();
  if(n==null || !n.matches("[a-f0-9-]{36}\\.(jpg|mp4|webm|pdf|zip)"))throw new IllegalArgumentException();
  return new File(getContext().getCacheDir(),n);
 }
 public ParcelFileDescriptor openFile(Uri u,String mode)throws FileNotFoundException{
  if(mode.equals("r"))return ParcelFileDescriptor.open(file(u),ParcelFileDescriptor.MODE_READ_ONLY);
  if(!"capture".equals(u.getPathSegments().get(0)))throw new FileNotFoundException();
  return ParcelFileDescriptor.open(file(u),ParcelFileDescriptor.MODE_READ_WRITE|ParcelFileDescriptor.MODE_CREATE|ParcelFileDescriptor.MODE_TRUNCATE);
 }
 public String getType(Uri u){String n=file(u).getName();return n.endsWith(".pdf")?"application/pdf":n.endsWith(".zip")?"application/zip":n.endsWith(".mp4")?"video/mp4":n.endsWith(".webm")?"video/webm":"image/jpeg";}
 public Cursor query(Uri u,String[] cols,String s,String[] a,String order){
  if(cols==null)cols=new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE};
  MatrixCursor c=new MatrixCursor(cols); MatrixCursor.RowBuilder row=c.newRow();
  for(String col:cols)row.add(col.equals(OpenableColumns.SIZE)?file(u).length():col.equals(OpenableColumns.DISPLAY_NAME)?(getContext().getString(R.string.app_name)+"."+file(u).getName().split("\\.")[1]):null);
  return c;
 }
 public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
 public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
 public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
