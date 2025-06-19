package com.lenyan.lenaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 终止工具（作用是让自主规划智能体能够合理地中断）
 */
public class TerminateTool {

    private static final Logger log = LoggerFactory.getLogger(TerminateTool.class);

    @Tool(description = """
            Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.
            "When you have finished all the tasks, call this tool to end the work.
            """)
    public String doTerminate() {
        // 重置WebSearchTool的搜索次数计数器
        WebSearchTool.resetSearchCallCount();
        log.info("会话终止，已重置WebSearchTool的搜索次数计数器");
        
        return "任务结束";
    }
}
