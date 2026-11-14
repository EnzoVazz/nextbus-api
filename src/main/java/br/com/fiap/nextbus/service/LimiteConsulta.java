package br.com.fiap.nextbus.service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

public class LimiteConsulta {

    private final int maximo;
    private final long janelaMillis;
    private final ConcurrentHashMap<String, Deque<Long>> marcas = new ConcurrentHashMap<>();

    public LimiteConsulta(int maximo, long janelaMillis) {
        this.maximo = maximo;
        this.janelaMillis = janelaMillis;
    }

    public boolean permitir(String chave, long agora) {
        Deque<Long> fila = marcas.computeIfAbsent(chave, ignorar -> new ArrayDeque<>());
        synchronized (fila) {
            while (!fila.isEmpty() && agora - fila.peekFirst() >= janelaMillis) {
                fila.removeFirst();
            }
            if (fila.size() >= maximo) {
                return false;
            }
            fila.addLast(agora);
            return true;
        }
    }
}
