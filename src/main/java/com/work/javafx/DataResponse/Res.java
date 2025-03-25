package com.work.javafx.DataResponse;

public class Res {
    private String text;
    private Integer code;
    private String translate;
    private String requestId;
    private String message;
    public Res() {
    }

    public Res(String text, Integer code, String translate) {
        this.text = text;
        this.code = code;
        this.translate = translate;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getTranslate() {
        return translate;
    }

    public void setTranslate(String translate) {
        this.translate = translate;
    }


}