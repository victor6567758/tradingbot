package com.precioustech.fxtrading.events.notification.email;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EmailPayLoad {
    private final String subject;
    private final String body;
}
