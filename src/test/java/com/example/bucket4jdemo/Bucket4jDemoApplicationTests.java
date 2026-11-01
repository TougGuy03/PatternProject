package com.example.bucket4jdemo;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.local.SynchronizedBucket;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class Bucket4jDemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Bucket bucket;

    @Test
    void allowsFiveRequestsAndRejectsTheSixth() throws Exception {
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/api/test"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Запрос разрешён"));
        }

        mockMvc.perform(get("/api/test"))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string("Превышен лимит запросов"));
    }

    @Test
    void createsSynchronizedBucketBean() {
        org.junit.jupiter.api.Assertions.assertInstanceOf(SynchronizedBucket.class, bucket);
    }
}
