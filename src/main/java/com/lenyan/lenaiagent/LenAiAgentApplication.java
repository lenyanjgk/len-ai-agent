package com.lenyan.lenaiagent;

import org.springframework.ai.autoconfigure.vectorstore.pgvector.PgVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

//@EnableAsync
@SpringBootApplication(exclude = PgVectorStoreAutoConfiguration.class)
public class LenAiAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(LenAiAgentApplication.class, args);
    }

}
