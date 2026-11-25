package lab4.service;

/**
 * Неизменяемый снимок статистики для шаблона stats.html.
 * Класс с геттерами: шаблон читает stats.total как getTotal().
 */
public class StatsSnapshot {

    private final long total;
    private final long hits;
    private final Long lastClearTime;

    public StatsSnapshot(long total, long hits, Long lastClearTime) {
        this.total = total;
        this.hits = hits;
        this.lastClearTime = lastClearTime;
    }

    public long getTotal() {
        return total;
    }

    public long getHits() {
        return hits;
    }

    public long getMisses() {
        return total - hits;
    }

    public Long getLastClearTime() {
        return lastClearTime;
    }
}
