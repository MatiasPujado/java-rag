package dev.danvega.markets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.pdf.ParagraphPdfDocumentReader;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class IngestionService implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
  private final PgVectorStore vectorStore;
  private final EmbeddingModel embeddingModel;
  private final JdbcTemplate jdbcTemplate;
  private int dimensions;

  @Value("classpath:/docs/article_thebeatoct2024.pdf")
  private Resource marketPDF;

  public IngestionService(EmbeddingModel embeddingModel, JdbcTemplate jdbcTemplate) {
    this.embeddingModel = embeddingModel;
    this.jdbcTemplate = jdbcTemplate;
    this.dimensions = embeddingModel.dimensions();
    this.vectorStore = new PgVectorStore(jdbcTemplate, embeddingModel, dimensions);
  }

  @Override
  public void run(String... args) throws Exception {
    var pdfReader = new ParagraphPdfDocumentReader(marketPDF);
    TextSplitter textSplitter = new TokenTextSplitter();
    log.info("Embedding Model Dimensions: {}", dimensions);

    vectorStore.createObservationContextBuilder("vector_store")
      .withDimensions(dimensions)
      .build();

    vectorStore.accept(textSplitter.apply(pdfReader.get()));
    log.info("VectorStore Loaded with data!");
  }
}
