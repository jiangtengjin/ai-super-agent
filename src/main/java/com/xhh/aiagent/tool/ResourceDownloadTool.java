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

    @Tool(description = "Download resource from the url")
    public String doDownload(@ToolParam(description = "The url of the download resource") String url,
                             @ToolParam(description = "The resource of name") String fileName){
        String fileDir = FileConstant.FILE_SAVE_DIR + File.separator + "download";
        String filePath = fileDir + File.separator + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 使用 downloadFile 方法下载资源
            HttpUtil.downloadFile(url, new File(filePath));
            return "Download resource successfully to: " + filePath;
        } catch (Exception e) {
            return "Fail to download resource: " + e.getMessage();
        }
    }

}
