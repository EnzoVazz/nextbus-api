package br.com.fiap.nextbus.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SentidoConverter implements AttributeConverter<Sentido, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Sentido sentido) {
        if (sentido == null) {
            return null;
        }
        return sentido.getCodigo();
    }

    @Override
    public Sentido convertToEntityAttribute(Integer codigo) {
        return Sentido.porCodigo(codigo);
    }
}
