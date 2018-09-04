package com.bsoft.deploy.controller;

import com.alibaba.fastjson.JSON;
import com.bsoft.deploy.context.Constant;
import com.bsoft.deploy.context.Global;
import com.bsoft.deploy.dao.entity.App;
import com.bsoft.deploy.dao.entity.AppPackage;
import com.bsoft.deploy.http.HttpResult;
import com.bsoft.deploy.service.AppService;
import com.bsoft.deploy.service.SlaveService;
import com.bsoft.deploy.utils.FileUtils;
import com.bsoft.deploy.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * 主节点restful api
 * Created on 2018/8/20.
 *
 * @author yangl
 */
@RestController
@RequestMapping("/app")
public class MasterController {
    private final static Logger logger = LoggerFactory.getLogger(MasterController.class);

    @Autowired
    AppService appService;

    @Autowired
    SlaveService slaveService;

    /**
     * 统一异常处理
     *
     * @param e
     * @return
     */
    @ExceptionHandler
    public HttpResult exceptionHandler(Exception e) {
        String error = "系统发生了意外的状况,反馈给管理员,我们将及时处理!";
        logger.error("error", e);
        return new HttpResult(30000, error);
    }


    /**
     * 获取应用列表
     *
     * @return
     */
    @RequestMapping(value = {"/list"}, method = RequestMethod.GET)
    public HttpResult appList() {
        List<App> apps = appService.loadApps();
        return new HttpResult(apps);
    }

    @RequestMapping(value = {"/delete"}, method = RequestMethod.GET)
    public HttpResult deleteApp(int appId) {
        // 判断应用是否被使用
        int size = slaveService.loadSlaves(appId).size();
        HttpResult result = new HttpResult();
        if (size > 0) {
            result.setCode(30001);
            result.setMessage("应用已经被启用,请先删除节点引用信息!");
            return result;
        }
        appService.deleteApp(appId);
        return new HttpResult();
    }

    /**
     * 应用 新增 or 修改
     *
     * @return
     */
    @RequestMapping(value = {"/create", "/update"}, method = RequestMethod.POST)
    public HttpResult saveApp(@RequestBody String jsonData) {
        App app = JSON.parseObject(jsonData, App.class);
        if (StringUtils.isEmpty(app.getAppId()) || app.getAppId() == 0) {
            appService.saveApp(app);
        } else {
            appService.updateApp(app);
        }
        Global.getAppStore().reloadAll();
        return new HttpResult(app);
    }

    /**
     * 初始化应用,设置初始版本
     *
     * @param appId
     * @return
     */
    @RequestMapping(value = {"/init"}, method = RequestMethod.GET)
    public HttpResult saveApp(int appId) {

        return new HttpResult();
    }


    /**
     * 获取应用更新包列表
     *
     * @return
     */
    @RequestMapping(value = {"/packages"}, method = RequestMethod.GET)
    public HttpResult appPackageList(int appId) {
        List<AppPackage> pkgs = appService.loadAppPackages(appId);
        return new HttpResult(pkgs);
    }

    /**
     * 上传更新包(支持zip压缩包上传)
     *
     * @return
     */
    @RequestMapping(value = {"/upload"}, method = RequestMethod.POST)
    @ResponseBody
    public void uploadPackageFiles(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 更新包id
        String pkgId = request.getParameter("pkgId");
        // 应用id
        String appId = request.getParameter("appId");
        // 当前块序号
        String chunkNumber = request.getParameter("chunkNumber");
        // 总块数
        String totalChunks = request.getParameter("totalChunks");
        // 分块大小
        String chunkSize = request.getParameter("chunkSize");
        // 总大小
        String totalSize = request.getParameter("totalSize");
        // 文件唯一标识
        String identifier = request.getParameter("identifier");
        // 文件名
        String filename = request.getParameter("filename");
        // 上传文件的相对路径
        String relative_path = request.getParameter("relativePath");
        // 文件对象
        MultipartFile file = ((MultipartHttpServletRequest) request).getFile("file");

        System.out.println("receive chunkNumber:" + chunkNumber + ",totalChunks:" + totalChunks);
        BufferedOutputStream fos = null;
        try {
            // 确定上传路径
            String app_home = Global.getAppStore().getApp(Integer.parseInt(appId)).getPath() + File.separator + "version_" + pkgId + File.separator;
            String file_path = StringUtils.cleanPath(app_home + relative_path);
            if (StringUtils.equals("1", totalChunks)) {
                File toFile = new File(file_path);
                if (!toFile.exists()) {
                    String dir = FileUtils.getFilePath(file_path);
                    new File(dir).mkdirs();
                }
                // 写入文件
                fos = new BufferedOutputStream(new FileOutputStream(toFile));
                fos.write(file.getBytes());
                fos.flush();
            } else {
                // 分片存储tmp
                String temp_home = Global.getAppStore().getApp(Integer.parseInt(appId)).getPath()
                        + File.separator + "temp"
                        + File.separator + identifier;
                File temp_dir = new File(temp_home);
                if (!temp_dir.exists()) {
                    temp_dir.mkdirs();
                }
                File tempFile = new File(temp_home + File.separator + chunkNumber + Constant.TMP_FILE_SUFFIX);
                fos = new BufferedOutputStream(new FileOutputStream(tempFile));
                fos.write(file.getBytes());
                fos.flush();
                // 合并并删除tmp
                if (temp_dir.list().length == Integer.parseInt(totalChunks)) {
                    FileUtils.closeStream(fos);
                    combineFiles(temp_dir, file_path);
                    FileUtils.deleteDir(temp_dir);
                }
            }
        } catch (Exception e) {
            logger.error("文件[{}]上传失败!", relative_path, e);
            response.sendError(415, e.getMessage());
        } finally {
            FileUtils.closeStream(fos);
        }


    }

    private void combineFiles(File tempDir, String filePath) throws IOException {
        File toFile = new File(filePath);
        if (!toFile.exists()) {
            String dir = FileUtils.getFilePath(filePath);
            new File(dir).mkdirs();
        }
        File outputFile = new File(filePath);

        if (!outputFile.exists()) {
            outputFile.createNewFile();
        }
        FileChannel outChannel = new FileOutputStream(outputFile).getChannel();
        FileChannel inChannel;
        for (int index=1;index<tempDir.list().length + 1; index++) {
            File file = new File(tempDir.getAbsolutePath() + File.separator + index + Constant.TMP_FILE_SUFFIX);
            inChannel = new FileInputStream(file).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            inChannel.close();
        }
        outChannel.close();
    }

}
