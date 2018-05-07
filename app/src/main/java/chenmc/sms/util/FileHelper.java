package chenmc.sms.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author 明 明
 *         Created on 2017-5-1.
 */

public class FileHelper {
    
    /**
     * 复制文件
     *
     * @param src 源文件
     * @param desDir 目标文件夹
     * @return 复制成功返回 true，否则返回 false
     */
    public static boolean copyFile(File src, File desDir) {
        if (desDir == null || (!desDir.exists() && !desDir.mkdirs()))
            return false;
        if (src.getParent().equals(desDir.getPath()))
            return true;
        
        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        try {
            input = new BufferedInputStream(
                new FileInputStream(src)
            );
            String[] split = src.getName().split("/");
            output = new BufferedOutputStream(
                new FileOutputStream(new File(desDir, split[split.length - 1]))
            );
            
            byte b[] = new byte[1024];
            while (input.available() > 0) {
                int len = input.read(b);
                output.write(b, 0, len);
            }
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return false;
    }
    
    public static File renameFile(File file, String newFilename) {
        File dest = new File(file.getParentFile(), newFilename);
        if (file.renameTo(dest)) return dest;
        else return file;
    }
}
