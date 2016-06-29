package com.ae.sat.message;

import com.ae.sat.model.Answer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ae on 24-5-16.
 */
public class Message {

    private byte[] content;

    private ContentType contentType;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final Logger log = LoggerFactory.getLogger(getClass());

    public Message() {}

    public Message(Answer answer) {
        contentType = ContentType.ANSWER;
        try {
            setContent(objectMapper.writeValueAsBytes(answer));
        } catch (JsonProcessingException e) {
            setContentType(null);
            log.error("Could not create message", e);
        }
    }

    public Message(Stats stats) {
        contentType = ContentType.STATISTICS;
        try {
            setContent(objectMapper.writeValueAsBytes(stats));
        } catch (JsonProcessingException e) {
            setContentType(null);
            log.error("Could not create message", e);
        }
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }
}
