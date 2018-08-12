package com.bsoft.deploy.file;

import com.bsoft.deploy.dao.entity.AppFile;
import com.bsoft.deploy.exception.FileOperationException;
import com.bsoft.deploy.utils.DateUtils;
import com.bsoft.deploy.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 接收master传输的文件
 * Created on 2018/8/10.
 *
 * @author yangl
 */
public class FileWoker {
    private String appPath = "D:/workspace_ideal/deploy/master_target/";
    private String backup_dir = "D:/workspace_ideal/deploy/master_backup/";

    public boolean receive(AppFile file) throws Exception {
        File baseDir = new File(appPath);
        if (!baseDir.exists()) {
            if (!baseDir.mkdirs()) {
                throw new FileOperationException("文件操作失败:应用基础路径不存在且创建失败!");
            }
        }

        File f = new File(appPath + file.getRelative());
        // 文件存在先备份
        if (f.exists()) {
            String today = DateUtils.getNow("yyyyMMdd");
            String backup_file_path = backup_dir + today;
            File backup_today = new File(backup_file_path);
            if (!backup_today.exists()) {
                if (!backup_today.mkdirs()) {
                    return false;
                }
            }
            // 保存备份文件
            File backup_file = new File(backup_file_path + File.separator + file.getName());
            FileUtils.copyFile(f, backup_file);

            // 文件是否允许操作
            if (!f.canWrite()) {
                throw new FileOperationException("文件操作失败:目标文件不允许写入!");
            }

        } else {
            String dir = FileUtils.getFilePath(f.getAbsolutePath());
            new File(dir).mkdirs();
        }
        // 更新文件
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(f);
            fos.write(file.getContent());
            fos.flush();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

        return true;
    }
}
