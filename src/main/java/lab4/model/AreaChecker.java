package lab4.model;

/**
 * Область варианта, границы входят:
 *   I   — треугольник (0,0), (R,0), (0,R): ниже прямой x + y = R
 *   II  — прямоугольник: -R <= x <= 0, 0 <= y <= R
 *   III — четверть круга радиуса R/2
 *   IV  — пусто
 */
public final class AreaChecker {

    private AreaChecker() {
    }

    public static boolean isHit(Point point) {
        double x = point.x();
        double y = point.y();
        double r = point.r();
        if (x >= 0 && y >= 0) return x + y <= r;
        if (x <= 0 && y >= 0) return x >= -r && y <= r;
        if (x <= 0 && y <= 0) return x * x + y * y <= (r / 2) * (r / 2);
        return false;
    }
}
