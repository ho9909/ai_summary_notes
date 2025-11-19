package com.example.ainote.note;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class NoteControllerTest {

        @Autowired
        MockMvc mvc;
        @Autowired
        ObjectMapper om;

        private static final String H = "X-USER-ID";

        @Test
        void create_list_detail_update_delete_and_tag_filter() throws Exception {
                // create
                var body = Map.of("title", "테스트", "contentMd", "# 본문", "tags", "ai,spring");
                var res = mvc.perform(post("/api/notes")
                                .header(H, "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(body)))
                                .andExpect(status().isOk())
                                .andReturn();
                long id = Long.parseLong(res.getResponse().getContentAsString().trim());

                // list
                mvc.perform(get("/api/notes?page=0&size=5").header(H, "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].title", is("테스트")))
                                .andExpect(jsonPath("$.content[0].tags", is("ai,spring")));

                // detail
                mvc.perform(get("/api/notes/{id}", id).header(H, "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id", is((int) id)));

                // update (tags only)
                var patch = Map.of("tags", "ai,llm");
                mvc.perform(patch("/api/notes/{id}", id)
                                .header(H, "1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(om.writeValueAsString(patch)))
                                .andExpect(status().isOk());

                // list by tag
                mvc.perform(get("/api/notes?tag=llm").header(H, "1"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].tags", containsString("llm")));

                // delete
                mvc.perform(delete("/api/notes/{id}", id).header(H, "1"))
                                .andExpect(status().isOk());
        }

        @Test
        void require_header_user_id() throws Exception {
                mvc.perform(get("/api/notes"))
                                .andExpect(status().is4xxClientError())
                                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print());

        }
}
