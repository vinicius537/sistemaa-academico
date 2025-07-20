package com.university.academic.service;

/**
 * Interface para o Observer (observador) no padrão Observer.
 * Qualquer classe que queira ser notificada sobre mudanças em um Observable
 * deve implementar esta interface.
 */
public interface Observer {
    /**
     * Método chamado pelo Observable para notificar os observadores sobre uma mudança.
     * @param data Os dados relacionados à mudança que ocorreu.
     */
    void update(Object data);
}