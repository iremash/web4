package lab4.service;

import lab4.model.CheckResult;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * История проверок ОДНОГО пользователя (браузера) — аналог ResultsBean из лабы 3.
 * @SessionScope: Spring создаёт отдельный объект на каждую HTTP-сессию и хранит его
 * в самой сессии как атрибут "scopedTarget.sessionHistory". То есть список берётся из HTTP-сессии.
 * В контроллер же внедряется прокси: при каждом вызове метода он находит объект текущей сессии.
 */
@Component
@SessionScope
public class SessionHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    // ArrayList, а не List: поле должно быть Serializable, а интерфейс List этого не гарантирует
    private final ArrayList<CheckResult> results = new ArrayList<>();
    private long generation;   // эпоха StatsService, к которой относится список

    // synchronized: у одного пользователя могут идти параллельные запросы (две вкладки)
    public synchronized void add(CheckResult result, long resultGeneration) {
        if (resultGeneration < generation) {
            return;   // точку учли до очистки, а мы уже знаем о новой эпохе — она стёрта
        }
        sync(resultGeneration);
        results.add(0, result);   // новые сверху
    }

    /** Копия списка: шаблон перебирает её спокойно, даже если параллельно добавят строку. */
    public synchronized List<CheckResult> getResults(long currentGeneration) {
        sync(currentGeneration);
        return List.copyOf(results);
    }

    // Была очистка после того, как мы последний раз смотрели — список устарел
    private void sync(long currentGeneration) {
        if (currentGeneration > generation) {
            results.clear();
            generation = currentGeneration;
        }
    }
}
