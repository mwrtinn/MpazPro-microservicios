package cl.duoc.mpaz.usuarios.util;

/** Utilidades para RUT chilenos (mismas reglas que valida la app móvil). */
public final class RutUtil {

    private RutUtil() {
    }

    public static String normalizar(String rut) {
        if (rut == null) {
            return "";
        }
        return rut.replaceAll("[^0-9kK]", "").toUpperCase();
    }

    public static boolean esValido(String rutNormalizado) {
        if (rutNormalizado == null || rutNormalizado.length() < 8 || rutNormalizado.length() > 9) {
            return false;
        }
        String cuerpo = rutNormalizado.substring(0, rutNormalizado.length() - 1);
        char dv = rutNormalizado.charAt(rutNormalizado.length() - 1);
        if (!cuerpo.chars().allMatch(Character::isDigit)) {
            return false;
        }
        return calcularDv(cuerpo) == dv;
    }

    // Dígito verificador por módulo 11
    public static char calcularDv(String cuerpo) {
        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * factor;
            factor = (factor == 7) ? 2 : factor + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) {
            return '0';
        }
        if (resto == 10) {
            return 'K';
        }
        return (char) ('0' + resto);
    }
}