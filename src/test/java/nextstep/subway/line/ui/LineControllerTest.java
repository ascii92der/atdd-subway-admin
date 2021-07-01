package nextstep.subway.line.ui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nextstep.subway.line.application.LineService;
import nextstep.subway.line.domain.Line;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@WebMvcTest
@SpringBootTest
@AutoConfigureMockMvc
class LineControllerTest {

    //    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LineService lineService;

    @Autowired
    private WebApplicationContext wac;

    @BeforeEach
    void beforeEach() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(print())
                .build();
    }

    @Test
    void createLine() throws Exception {

        // given
        LineRequest lineRequest = new LineRequest("2호선", "green");
        LineResponse lineResponse = LineResponse.of(new Line("2호선", "green"));
        when(lineService.saveLine(any(LineRequest.class)))
                .thenReturn(lineResponse);

        // when
        // then
        mockMvc.perform(
                post("/lines")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lineRequest))
        ).andExpect(status().isCreated());
    }

    @Test
    void createDuplicatedLine() throws Exception {
        //given
        LineRequest lineRequest = new LineRequest("2호선", "green");
        when(lineService.saveLine(any(LineRequest.class))).thenThrow(DataIntegrityViolationException.class);

        //when
        //then
        mockMvc.perform(
                post("/lines")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(lineRequest))
        ).andExpect(status().isConflict());
    }

    @Test
    void showLines() throws Exception {
        //given
        LineResponse lineResponse1 = LineResponse.of(new Line("2호선", "green"));
        LineResponse lineResponse2 = LineResponse.of(new Line("3호선", "orange"));
        List<LineResponse> lineResponseList = Arrays.asList(lineResponse1, lineResponse2);
        when(lineService.findAllLines()).thenReturn(lineResponseList);

        //when
        //then
        mockMvc.perform(
                get("/lines")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(lineResponseList)));
    }

    @Test
    void showLine() throws Exception {
        //given
        long lineId = 1L;
        LineResponse lineResponse = LineResponse.of(new Line("2호선", "green"));
        when(lineService.findLine(any())).thenReturn(lineResponse);

        //when
        //then
        mockMvc.perform(
                get("/lines/" + lineId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk())
                .andExpect(content().string(objectMapper.writeValueAsString(lineResponse)));
    }

    @Test
    void noSuchElementInShowLine() throws Exception {
        //given
        long lineId = 1L;
        when(lineService.findLine(any())).thenThrow(NoSuchElementException.class);

        //when
        //then
        mockMvc.perform(
                get("/lines/" + lineId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent());

    }
}