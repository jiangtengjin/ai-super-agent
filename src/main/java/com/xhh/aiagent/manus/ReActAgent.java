package com.xhh.aiagent.manus;


import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *  ReAct（Reasoning and acting） 模式的代理抽象类
 *  实现了思考 - 行动的循环模式
 */
@EqualsAndHashCode(callSuper = true)
@Data
public abstract class ReActAgent extends BaseAgent{

    /**
     *  处理当前状态并决定下一步行动
     *
     * @return 是否需要行动， true 表示需要， false 表示不需要
     */
    protected abstract boolean think();

    /**
     * 执行决定的行动
     *
     * @return  行动执行结果
     */
    protected abstract String act();

    /**
     * 实现父类的 step 方法
     * think + act
     *
     * @return 当前步骤的执行结果
     */
    @Override
    protected String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                // 不需要执行
                return "Thinking complete - no action needed";
            }
            return act();
        } catch (Exception e) {
            e.printStackTrace();
            return "Agent step error: " + e.getMessage();
        }
    }
}
