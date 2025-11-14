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

    @Tool(description = "Read the file content")
    public String readFile(@ToolParam(description = "The file name of the content that needs to be read") String fileName) {
        String filePath = FILE_DIR + File.separator + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (IORuntimeException e) {
            return "Fail to read file：" + e.getMessage();
        }
    }

    @Tool(description = "Write file")
    public String writeFile(@ToolParam(description = "The file name that needs to be written") String fileName,
                            @ToolParam(description = "The content that needs to be written") String content) {
        String filePath = FILE_DIR + File.separator + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content, filePath);
            return "Write file successfully to: " + filePath;
        } catch (IORuntimeException e) {
            return "Fail to write file：" + e.getMessage();
        }
    }

}
