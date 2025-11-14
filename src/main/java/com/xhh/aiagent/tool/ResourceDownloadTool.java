package com.xhh.aiagent.tool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.xhh.aiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

/**
 * 资源下载工具
 */
public class ResourceDownloadTool {

    @Tool(description = "从 url 下载资源")
    public String doDownload(@ToolParam(description = "下载资源的 url 地址") String url,
                             @ToolParam(description = "资源的文件名称") String fileName){
        String fileDir = FileConstant.FILE_SAVE_DIR + File.separator + "download";
        String filePath = fileDir + File.separator + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 使用 downloadFile 方法下载资源
            HttpUtil.downloadFile(url, new File(filePath));
            return "资源下载成功，保存路径：" + filePath;
        } catch (Exception e) {
            return "资源下载失败：" + e.getMessage();
        }
    }

}
