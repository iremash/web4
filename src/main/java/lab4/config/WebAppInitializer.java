package lab4.config;

import jakarta.servlet.Filter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * Замена servlet/servlet-mapping из web.xml лабы 3.
 * WildFly при старте находит этот класс сам: в spring-web есть ServletContainerInitializer
 * (стандартный механизм Servlet 3.0+), он ищет наследников WebApplicationInitializer и вызывает их.
 * Здесь регистрируется один DispatcherServlet — Front Controller Spring MVC
 * (то, что в лабе 3 делал наш ControllerServlet).
 */
public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    // Общий (root) контекст не нужен: всё приложение — веб-слой
    @Override
    protected Class<?>[] getRootConfigClasses() {
        return null;
    }

    // Конфигурация контекста DispatcherServlet: контроллеры, Thymeleaf, статика
    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[]{WebConfig.class};
    }

    // "/" — DispatcherServlet получает все запросы, которые не забрал другой сервлет.
    // Статику (css, js) он тоже получает и отдаёт через ResourceHandler из WebConfig.
    @Override
    protected String[] getServletMappings() {
        return new String[]{"/"};
    }

    // Все параметры запроса и ответ — в UTF-8 (иначе русский текст может превратиться в «кракозябры»)
    @Override
    protected Filter[] getServletFilters() {
        return new Filter[]{new CharacterEncodingFilter("UTF-8", true)};
    }
}
