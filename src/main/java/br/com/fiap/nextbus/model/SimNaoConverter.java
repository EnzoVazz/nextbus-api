package br.com.fiap.nextbus.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SimNaoConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean ativa) {
        if (ativa == null) {
            return null;
        }
        return ativa ? "S" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String valor) {
        if (valor == null) {
            return null;
        }
        return "S".equalsIgnoreCase(valor.trim());
    }
}
