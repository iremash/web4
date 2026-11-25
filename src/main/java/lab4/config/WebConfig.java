package lab4.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * Конфигурация Spring MVC.
 *   @EnableWebMvc   — включает стандартную инфраструктуру MVC (разбор @GetMapping, @RequestParam и т.д.)
 *   @ComponentScan  — Spring сам находит классы с @Controller / @Service / @Component и создаёт их
 * Цепочка View: контроллер возвращает имя "main" -> ThymeleafViewResolver ->
 * шаблон /WEB-INF/templates/main.html -> HTML в ответ.
 */
@Configuration
@EnableWebMvc
@ComponentScan({"lab4.controller", "lab4.service"})
public class WebConfig implements WebMvcConfigurer {

    // Где искать шаблоны: имя вида "main" превращается в /WEB-INF/templates/main.html.
    // WEB-INF напрямую из браузера недоступна, как и JSP в лабе 3.
    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("/WEB-INF/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(true);   // шаблон разбирается один раз, потом берётся из кэша
        return resolver;
    }

    // Движок: подставляет данные модели в шаблон. Выражения ${...} считает Spring EL.
    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        engine.setEnableSpringELCompiler(true);
        return engine;
    }

    @Override
    public void configureViewResolvers(ViewResolverRegistry registry) {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setCharacterEncoding("UTF-8");
        resolver.setContentType("text/html;charset=UTF-8");
        registry.viewResolver(resolver);
    }

    // DispatcherServlet висит на "/", поэтому статику надо отдать явно:
    // запрос /lab4/static/style.css -> файл src/main/webapp/static/style.css
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("/static/");
    }
}
