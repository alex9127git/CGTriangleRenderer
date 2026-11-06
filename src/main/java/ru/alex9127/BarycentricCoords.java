package ru.alex9127;

public record BarycentricCoords(double alpha, double beta, double gamma) {
    public static BarycentricCoords of(double x, double y, Point a, Point b, Point c) {
        double d = doubledArea(a.x(), a.y(), b.x(), b.y(), c.x(), c.y());
        double d1 = doubledArea(x, y, b.x(), b.y(), c.x(), c.y());
        double d2 = doubledArea(a.x(), a.y(), x, y, c.x(), c.y());
        double d3 = doubledArea(a.x(), a.y(), b.x(), b.y(), x, y);
        assert d1 / d >= 0;
        assert d2 / d >= 0;
        assert d3 / d >= 0;
        return new BarycentricCoords(d1 / d, d2 / d, d3 / d);
    }

    private static double doubledArea(double x1, double y1, double x2, double y2, double x3, double y3) {
        return x1 * y2 + x2 * y3 + x3 * y1 - x3 * y2 - x1 * y3 - x2 * y1;
    }
}
