package com.example.oneonone.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.oneonone.model.OneOnOne;
import com.example.oneonone.model.OneOnOneHistory;
import com.example.oneonone.model.OneOnOneStatus;
import com.example.oneonone.repository.OneOnOneHistoryRepository;
import com.example.oneonone.repository.OneOnOneRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OneOnOneControllerHistoryTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OneOnOneRepository oneOnOneRepository;

    @Autowired
    private OneOnOneHistoryRepository historyRepository;

    @Test
    void fetchHistoryReturnsLatestFirst() throws Exception {
        OneOnOne oneOnOne = oneOnOneRepository.save(OneOnOne.builder()
                .title("Quarterly Review")
                .organizer("Manager A")
                .participant("Member A")
                .startAt(LocalDateTime.now().plusDays(1).withNano(0))
                .endAt(LocalDateTime.now().plusDays(1).plusHours(1).withNano(0))
                .location("Room A")
                .status(OneOnOneStatus.SCHEDULED)
                .tags("goals")
                .notes("Initial schedule")
                .build());

        historyRepository.save(OneOnOneHistory.builder()
                .oneOnOneId(oneOnOne.getId())
                .status(OneOnOneStatus.SCHEDULED)
                .changedBy("Manager A")
                .changedAt(LocalDateTime.now().minusDays(1).withNano(0))
                .comment("Scheduled")
                .build());

        historyRepository.save(OneOnOneHistory.builder()
                .oneOnOneId(oneOnOne.getId())
                .status(OneOnOneStatus.COMPLETED)
                .changedBy("Manager A")
                .changedAt(LocalDateTime.now().withNano(0))
                .comment("Completed")
                .build());

        mockMvc.perform(get("/api/oneonones/" + oneOnOne.getId() + "/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$[0].changedBy").value("Manager A"))
                .andExpect(jsonPath("$[0].comment").value("Completed"))
                .andExpect(jsonPath("$[1].status").value("SCHEDULED"))
                .andExpect(jsonPath("$[1].comment").value("Scheduled"));
    }

    @Test
    void fetchHistoryReturnsNotFoundForMissingOneOnOne() throws Exception {
        mockMvc.perform(get("/api/oneonones/999999/history"))
                .andExpect(status().isNotFound());
    }
}
