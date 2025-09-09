package com.onclass.bootcamp.domain.exceptions;

import com.onclass.bootcamp.domain.enums.TechnicalMessage;
import lombok.Getter;

@Getter
public class ProcessorException extends RuntimeException {

    private final TechnicalMessage technicalMessage;

    public ProcessorException(Throwable cause, TechnicalMessage message) {
        super(cause);
        this.technicalMessage = message;
    }

    public ProcessorException(String message, TechnicalMessage technicalMessage) {
        super(message);
        this.technicalMessage = technicalMessage;
    }
}
