package lab4.service;

import org.springframework.stereotype.Service;

/**
 * Общее состояние приложения: счётчики по ВСЕМ точкам всех пользователей.
 * @Service без указания scope = singleton: Spring создаёт один объект на всё приложение,
 * поэтому все запросы (все потоки) работают с ним одновременно — отсюда synchronized.
 *
 * generation — номер «эпохи»: растёт при каждой очистке. Историю сессий напрямую
 * не очистить (чужие сессии нам недоступны), поэтому каждая SessionHistory помнит,
 * к какой эпохе относится её список, и сама сбрасывается, увидев номер новее.
 */
@Service
public class StatsService {

    private long total;
    private long hits;
    private Long lastClearTime;   // мс UTC; null — очистки ещё не было
    private long generation;

    /** Учесть новую точку. Возвращает эпоху, в которой она учтена. */
    public synchronized long register(boolean hit) {
        total++;
        if (hit) hits++;
        return generation;
    }

    /** Очистить всё: счётчики в ноль, время очистки, новая эпоха (истории сессий станут пустыми). */
    public synchronized void clear() {
        total = 0;
        hits = 0;
        lastClearTime = System.currentTimeMillis();
        generation++;
    }

    public synchronized long getGeneration() {
        return generation;
    }

    /** Согласованный снимок: все числа прочитаны под одной блокировкой. */
    public synchronized StatsSnapshot snapshot() {
        return new StatsSnapshot(total, hits, lastClearTime);
    }
}
