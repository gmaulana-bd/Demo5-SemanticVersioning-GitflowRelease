package com.axa.demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AppTest {
    @Test
    void greetingIsCorrect() {
        assertEquals("AXA release demo", new App().greeting());
    }
}
