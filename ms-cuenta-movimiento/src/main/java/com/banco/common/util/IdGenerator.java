package com.banco.common.util;

import java.util.UUID;

public final class IdGenerator {

    private IdGenerator() {
        throw new AssertionError("Clase utilitaria no instanciable");
    }

    private static String random(int length) {
        return UUID.randomUUID()
                   .toString()
                   .replace("-", "")
                   .substring(0, length)
                   .toUpperCase();
    }

    public static String generateClienteId() {
        return "CLI-" + random(8);
    }

    public static String generateNumeroCuenta() {
        return "CTA-" + random(8);
    }

    public static String generateMovimientoId() {
        return "MOV-" + random(12);
    }

    public static boolean isValidFormat(String id, String prefix) {
        if (id == null || !id.startsWith(prefix + "-")) {
            return false;
        }
        String uuid = id.substring(prefix.length() + 1);
        return uuid.matches("^[A-F0-9]{8,12}$");
    }
}
