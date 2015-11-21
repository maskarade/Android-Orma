package com.github.gfx.android.orma.processor;

import javax.lang.model.element.Element;

public class ProcessingException extends RuntimeException {

    public final Element element;

    public ProcessingException(String message, Element element, Throwable throwable) {
        super(message, throwable);
        this.element = element;
    }
    public ProcessingException(String message, Element element) {
        super(message);
        this.element = element;
    }
}
