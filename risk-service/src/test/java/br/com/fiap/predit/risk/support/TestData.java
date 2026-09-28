package br.com.fiap.predit.risk.support;

import java.util.concurrent.ThreadLocalRandom;

public final class TestData {
    private TestData() {}

    /** VIN valido (17 caracteres, sem I, O e Q) e unico por chamada. */
    public static String vin() {
        return "9BF" + String.format("%014d", ThreadLocalRandom.current().nextLong(100_000_000_000_000L));
    }

    public static String customerJson(String email, String vin, boolean consent) {
        return """
                {"name":"Cliente Teste","email":"%s","phone":"+5511999999999","dealership":"Ford Lapa",
                 "contactConsent":%s,"vin":"%s","model":"Ranger","modelYear":2024,"mileage":15000}"""
                .formatted(email, consent, vin);
    }

    public static String uniqueEmail() {
        return "cliente" + System.nanoTime() + "@example.com";
    }
}
