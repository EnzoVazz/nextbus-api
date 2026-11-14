package br.com.fiap.nextbus.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ConsultaCache<T> {

    private static final long QUINZE_SEGUNDOS = 15_000;
    private final ConcurrentHashMap<String, Item<T>> itens = new ConcurrentHashMap<>();

    public T obter(String chave, Supplier<T> origem) {
        long agora = System.currentTimeMillis();
        Item<T> atual = itens.get(chave);
        if (atual != null && atual.expira() > agora) {
            return atual.valor();
        }
        T valor = origem.get();
        itens.put(chave, new Item<>(valor, agora + QUINZE_SEGUNDOS));
        return valor;
    }

    private record Item<T>(T valor, long expira) {
    }
}
