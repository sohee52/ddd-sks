package com.back.exception;

import lombok.Getter;

@Getter
public class DomainException extends RuntimeException {
    private final String resultCode;
    private final String msg;

    public DomainException(String resultCode, String msg) {
        super(resultCode + " : " + msg); // RuntimeException의 생성자는 메시지를 인자로 받음
        this.resultCode = resultCode;
        this.msg = msg;
    }
}