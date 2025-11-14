package com.xhh.aiagent.tool;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import com.xhh.aiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

/**
 * 文件操作工具
 */
public class FileOperationTool {

    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + File.separator + "file";

    @Tool(description = "读取文件内容")
    public String readFile(@ToolParam(description = "需要读取内容的文件名") String fileName) {
        String filePath = FILE_DIR + File.separator + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (IORuntimeException e) {
            return "文件读取失败：" + e.getMessage();
        }
    }

    @Tool(description = "写入文件")
    public String writeFile(@ToolParam(description = "需要写入内容的文件名") String fileName,
                            @ToolParam(description = "需要写入的内容") String content) {
        String filePath = FILE_DIR + File.separator + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "文件成功写入：" + filePath;
        } catch (IORuntimeException e) {
            return "文件写入失败：" + e.getMessage();
        }
    }

}
