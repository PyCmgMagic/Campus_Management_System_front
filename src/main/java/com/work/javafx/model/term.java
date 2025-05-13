package com.work.javafx.model;

public  class term{
    private String term;
    private boolean open;

    public term() {
    }

    public term(String term, boolean open) {
        this.term = term;
        this.open = open;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}