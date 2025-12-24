package com.back.global.rsData;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RsData<T> {
    private final String resultCode;
    private final String msg;
    private final T data; // Post, Member 등 다양한 타입이 올 수 있으므로 제네릭으로 처리

    public RsData(String resultCode, String msg) {
        this(resultCode, msg, null);
    }
}
