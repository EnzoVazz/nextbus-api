package br.com.fiap.nextbus.service;

public final class Distancia {

    private Distancia() {
    }

    public static int metros(double lat1, double lng1, double lat2, double lng2) {
        double raio = 6_371_000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return (int) Math.round(2 * raio * Math.asin(Math.sqrt(a)));
    }
}
