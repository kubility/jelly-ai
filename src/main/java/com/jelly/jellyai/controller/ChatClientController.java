package com.jelly.jellyai.controller;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.jelly.jellyai.advisor.MsgChatAdvisor;
import com.jelly.jellyai.entity.ChatBase;
import com.jelly.jellyai.entity.SysChatEntity;
import com.jelly.jellyai.tools.DateTimeTools;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/client")
@Tag(name = "ChatClient")
public class ChatClientController {


    @Autowired
    OllamaChatModel ollamaChatModel;
    @Autowired
    DashScopeChatModel dashscopeChatModel;
    @Autowired
    private ChatMemory chatMemory;
    @Autowired
    @Qualifier("redisChatMemory")
    private ChatMemory redisChatMemory;
    @Autowired
    private MsgChatAdvisor msgChatAdvisor;
    @Value("classpath:prompts/system.st")
    private Resource systemResource;
    @Value("${spring.ai.ollama.chat.options.model}")
    private String model;
    @Autowired
    private VectorStore vectorStore;

    @PostMapping("/ollama/call")
    @Operation(summary = "ollama call POST请求")
    public String ollama_call(@RequestBody SysChatEntity chat) {
        //创建系统提示
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        Message systemMessage = systemPromptTemplate.createMessage();
        //创建用户提示
        Message userMessage =new UserMessage(chat.getPrompt());
        Prompt prompt = new Prompt(List.of(userMessage, systemMessage));
        ChatClient.ChatClientRequestSpec chatClientRequestSpec = ChatClient.builder(ollamaChatModel).build().prompt(prompt)
                .advisors(msgChatAdvisor)
                .tools(new DateTimeTools());
        ChatOptions.Builder options = ChatOptions.builder();
        if (StringUtils.isNotBlank(chat.getModel())) {
            options.model(chat.getModel());
        }
        if (chat.getTemperature() != null) {
            options.temperature(chat.getTemperature());
        }
        if (chat.getMax_tokens() != null) {
            options.maxTokens(chat.getMax_tokens());
        }
        return chatClientRequestSpec
                .options(options.build())
                //.user(chat.getPrompt())
                .call().content();
    }

    @PostMapping(value = "/ollama/stream")
    @Operation(summary = "ollama stream POST请求")
    public Flux<String> ollama_stream(@RequestBody ChatBase chat) {
        ChatClient.ChatClientRequestSpec chatClientRequestSpec = ChatClient.builder(ollamaChatModel).build().prompt()
                .advisors(msgChatAdvisor, MessageChatMemoryAdvisor.builder(chatMemory).conversationId("v1").build());
        ChatOptions.Builder options = ChatOptions.builder();
        if (StringUtils.isNotBlank(chat.getModel())) {
            options.model(chat.getModel());
        }
        if (chat.getTemperature() != null) {
            options.temperature(chat.getTemperature());
        }
        if (chat.getMax_tokens() != null) {
            options.maxTokens(chat.getMax_tokens());
        }
        return chatClientRequestSpec
                .options(options.build())
                .user(chat.getPrompt())
                .stream().content();
    }

    @GetMapping( "/ollama/stream")
    @Operation(summary = "ollama stream GET请求")
    public Flux<String> chatStream(@RequestParam("prompt") String prompt) {
        return  ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(
                    QuestionAnswerAdvisor.builder(vectorStore).build()
                )
                .build()
                .prompt(prompt)
                .stream().content();

    }

    record ActorsFilms(String actor, List<String> films){

    }
    @GetMapping(value = "/ollama/film",produces = "text/html;charset=utf-8")
    @Operation(summary = "ollama 结构化输出实体")
    public String actorsFilm(@RequestParam("actor") String actor) {
        ActorsFilms actorsFilms = ChatClient.builder(ollamaChatModel)
                .build()
                .prompt()
                .user(u->u.text("列出{actor}所演过的电影").param("actor",actor))
                .call().entity(ActorsFilms.class);
        return JSON.toJSONString(actorsFilms, JSONWriter.Feature.PrettyFormat);

    }
    @GetMapping(value = "/ollama/films",produces = "text/html;charset=utf-8")
    @Operation(summary = "ollama 结构化输出List")
    public String actorsFilms(@RequestParam("actor") String actor) {
        List<ActorsFilms> actorsFilms = ChatClient.builder(ollamaChatModel)
                .build()
                .prompt()
                .user(u->u.text("生成{actor}出演过的五部电影").param("actor",actor))
                .call()
                .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {});
        return JSON.toJSONString(actorsFilms, JSONWriter.Feature.PrettyFormat);

    }

    @PostMapping("/dashscope/ai")
    @Operation(summary = "dashscope请求")
    public String dashscope(@RequestBody ChatBase chat) {
        return ChatClient.builder(dashscopeChatModel).build().prompt()
                .user(chat.getPrompt())
                .call().content();
    }

}
