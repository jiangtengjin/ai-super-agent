package com.xhh.aiagent.manus;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MyManusTest {

    @Resource
    private MyManus myManus;

    @Test
    public void test() {
        String userPrompt = """  
                我周末想和对象去深圳的天文台玩，请帮我规划一下游玩的行程
                并结合一些网络图片，制定一份详细的行程书，
                并以 PDF 格式输出""";
        String result = myManus.run(userPrompt);
        Assertions.assertNotNull(result);
    }

}
