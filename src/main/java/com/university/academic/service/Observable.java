package com.university.academic.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe abstrata para o Observable (sujeito) no padrão Observer.
 * Classes que estendem esta classe podem registrar observadores e notificá-los
 * sobre mudanças de estado.
 */
public abstract class Observable {
    // Lista de observadores registrados que serão notificados
    private List<Observer> observers = new ArrayList<>();

    /**
     * Adiciona um observador à lista de observadores.
     * @param observer O observador a ser adicionado.
     */
    public void addObserver(Observer observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Remove um observador da lista de observadores.
     * @param observer O observador a ser removido.
     */
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    /**
     * Notifica todos os observadores registrados sobre uma mudança.
     * @param data Os dados que serão passados para os observadores.
     */
    public void notifyObservers(Object data) {
        // Percorre a lista de observadores e chama o método update de cada um.
        // Usamos uma cópia da lista para evitar ConcurrentModificationException
        // caso um observador se remova durante a notificação.
        for (Observer observer : new ArrayList<>(observers)) {
            observer.update(data);
        }
    }
}