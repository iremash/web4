package lab4.model;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Разбор и проверка параметров точки (бывший RequestParser из лабы 3).
 * Теперь получает готовые строки: доставать их из запроса — работа Spring (@RequestParam).
 * При любой ошибке бросает IllegalArgumentException с текстом для пользователя.
 */
public final class PointValidator {
    // public: те же списки контроллер отдаёт в шаблон, чтобы нарисовать кнопки X и пункты R
    public static final List<Integer> X_VALUES = List.of(-4, -3, -2, -1, 0, 1, 2, 3, 4);
    public static final List<Integer> R_VALUES = List.of(1, 2, 3, 4, 5);
    private static final double Y_MIN = -3;
    private static final double Y_MAX = 5;
    // Клик по графику даёт любые X и Y, для него проверяется только интервал
    private static final double CLICK_MIN = -5;
    private static final double CLICK_MAX = 5;
    private static final Pattern NUMBER = Pattern.compile("[-+]?\\d{1,2}(\\.\\d{1,10})?");

    private PointValidator() {
    }

    public static Point parse(String rawX, String rawY, String rawR, boolean fromClick) {
        double x = readNumber(rawX, "X");
        double y = readNumber(rawY, "Y");
        double r = readNumber(rawR, "R");

        if (fromClick) {
            checkInterval(x, CLICK_MIN, CLICK_MAX, "X точки с графика");
            checkInterval(y, CLICK_MIN, CLICK_MAX, "Y точки с графика");
        } else {
            if (!contains(X_VALUES, x)) {
                throw new IllegalArgumentException("X вне допустимых значений");
            }
            checkInterval(y, Y_MIN, Y_MAX, "Y");
        }
        if (!contains(R_VALUES, r)) {
            throw new IllegalArgumentException("R вне допустимых значений");
        }
        return new Point(x, y, r);
    }

    private static double readNumber(String raw, String name) {
        if (raw == null) {
            throw new IllegalArgumentException("Не передан параметр " + name);
        }
        String s = raw.trim().replace(',', '.');
        if (!NUMBER.matcher(s).matches()) {
            throw new IllegalArgumentException("Параметр " + name + " должен быть числом");
        }
        return Double.parseDouble(s);
    }

    // Интервал открытый: границы не входят
    private static void checkInterval(double value, double min, double max, String name) {
        if (!(value > min && value < max)) {
            throw new IllegalArgumentException(name + " должен быть в интервале (" + text(min) + ", " + text(max) + ")");
        }
    }

    // -3.0 -> "-3" для сообщений
    private static String text(double value) {
        return value == Math.rint(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static boolean contains(List<Integer> values, double value) {
        for (int v : values) {
            if (v == value) return true;
        }
        return false;
    }
}
