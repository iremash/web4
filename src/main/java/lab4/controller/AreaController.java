package lab4.controller;

import jakarta.servlet.http.HttpServletResponse;
import lab4.model.AreaChecker;
import lab4.model.CheckResult;
import lab4.model.Point;
import lab4.model.PointValidator;
import lab4.service.SessionHistory;
import lab4.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.logging.Logger;

/**
 * Страница №2: область, форма, таблица результатов.
 *   GET /main   — показать страницу
 *   GET /check  — проверить точку (метод GET по варианту), затем redirect на /main
 */
@Controller
public class AreaController {
    private static final Logger LOG = Logger.getLogger(AreaController.class.getName());

    private final StatsService stats;       // один на всё приложение
    private final SessionHistory history;   // прокси: за ним свой объект в каждой HTTP-сессии

    public AreaController(StatsService stats, SessionHistory history) {
        this.stats = stats;
        this.history = history;
    }

    @GetMapping("/main")
    public String mainPage(Model model) {
        fillModel(model);
        return "main";
    }

    @GetMapping("/check")
    public String check(@RequestParam(name = "x", required = false) String x,
                        @RequestParam(name = "y", required = false) String y,
                        @RequestParam(name = "r", required = false) String r,
                        @RequestParam(name = "source", required = false) String source,
                        Model model,
                        HttpServletResponse response,
                        RedirectAttributes redirect) {
        long start = System.nanoTime();
        LOG.info("запрос: x=" + x + ", y=" + y + ", r=" + r + ", source=" + source);

        try {
            Point point = PointValidator.parse(x, y, r, "click".equals(source));
            boolean hit = AreaChecker.isHit(point);
            long execMicros = (System.nanoTime() - start) / 1000;
            CheckResult result = new CheckResult(point, hit, System.currentTimeMillis(), execMicros);

            long generation = stats.register(hit);   // в общую статистику
            history.add(result, generation);         // в историю этой сессии

            // Flash-атрибут переживёт ровно один redirect: /main покажет вердикт и забудет его
            redirect.addFlashAttribute("last", result);
            LOG.info("ответ: hit=" + hit + ", " + execMicros + " мкс");
            // Post/Redirect/Get: в адресной строке останется /main, F5 не добавит точку второй раз
            return "redirect:/main";
        } catch (IllegalArgumentException e) {
            LOG.warning("отклонён: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("error", e.getMessage());
            fillModel(model);
            return "main";   // та же страница, но с кодом 400 и текстом ошибки
        }
    }

    // Всё, что нужно шаблону main.html
    private void fillModel(Model model) {
        List<CheckResult> results = history.getResults(stats.getGeneration());
        model.addAttribute("results", results);
        // R последней проверки выбран по умолчанию, чтобы на графике сразу была область и точки
        model.addAttribute("selectedR", results.isEmpty() ? null : (int) results.get(0).rValue());
        model.addAttribute("xValues", PointValidator.X_VALUES);
        model.addAttribute("rValues", PointValidator.R_VALUES);
    }
}
