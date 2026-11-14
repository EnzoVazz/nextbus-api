package br.com.fiap.nextbus.model;

public enum Sentido {
    IDA(1),
    VOLTA(2);

    private final int codigo;

    Sentido(int codigo) {
        this.codigo = codigo;
    }

    public int getCodigo() {
        return codigo;
    }

    public static Sentido porCodigo(Integer codigo) {
        if (codigo == null) {
            return null;
        }
        for (Sentido sentido : values()) {
            if (sentido.codigo == codigo) {
                return sentido;
            }
        }
        throw new IllegalArgumentException("Sentido invalido: " + codigo);
    }
}
