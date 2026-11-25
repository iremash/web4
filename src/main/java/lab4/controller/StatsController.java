package lab4.controller;

import lab4.service.StatsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.logging.Logger;

/**
 * Страница №1: общая статистика по всем точкам и очистка состояния.
 */
@Controller
public class StatsController {
    private static final Logger LOG = Logger.getLogger(StatsController.class.getName());

    private final StatsService stats;

    // Внедрение через конструктор: Spring сам передаст сюда единственный StatsService
    public StatsController(StatsService stats) {
        this.stats = stats;
    }

    @GetMapping("/")
    public String statsPage(Model model) {
        model.addAttribute("stats", stats.snapshot());
        return "stats";   // -> /WEB-INF/templates/stats.html
    }

    // POST, а не GET: запрос меняет состояние сервера. GET по варианту — для отправки точек.
    // После очистки — redirect (Post/Redirect/Get): обновление страницы не пошлёт POST повторно.
    @PostMapping("/clear")
    public String clear() {
        stats.clear();
        LOG.info("состояние очищено");
        return "redirect:/";
    }
}
