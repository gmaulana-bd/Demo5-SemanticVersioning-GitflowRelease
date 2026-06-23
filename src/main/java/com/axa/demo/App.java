package com.axa.demo;

/**
 * Trivial app for the release demo. The point of this demo is the RELEASE
 * PROCESS (versioning, branching, tagging, SBOM, publish), not the code.
 */
public class App {
    public String greeting() {
        return "AXA release demo";
    }

    public static void main(String[] args) {
        System.out.println(new App().greeting());
    }
}
