package com.lenyan.lenaiagent.config;

import com.lenyan.lenaiagent.rag.LoveAppDocumentLoader;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgDistanceType.COSINE_DISTANCE;
import static org.springframework.ai.vectorstore.pgvector.PgVectorStore.PgIndexType.HNSW;

@Configuration
public class PgVectorVectorStoreConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

//    @Bean
    public VectorStore pgVectorVectorStore(@Qualifier("postgresJdbcTemplate") JdbcTemplate jdbcTemplate, EmbeddingModel dashscopeEmbeddingModel) {
        // 创建PgVectorStore实例，配置向量存储的参数
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536)                    // 设置向量的维度，可选，默认为模型维度或1536
                .distanceType(COSINE_DISTANCE)       // 设置计算向量间距离的方法，可选，默认为余弦距离
                .indexType(HNSW)                     // 设置索引类型，可选，默认为HNSW（高效近似最近邻搜索）
                .initializeSchema(true)              // 是否初始化数据库模式，可选，默认为false
                .schemaName("public")                // 设置数据库模式名称，可选，默认为"public"
                .vectorTableName("vector_store")     // 设置存储向量数据的表名，可选，默认为"vector_store"
                .maxDocumentBatchSize(10000)         // 设置文档批量插入的最大数量，可选，默认为10000
                .build();
        // 加载文档
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        vectorStore.add(documents);
        return vectorStore;
    }
}
