package com.laysar.G2;

final class G2Point {
    static final G2Point Zero = new G2Point(0.0, 0.0);

    final double x;
    final double y;

    G2Point(double X, double Y) {
        x = X;
        y = Y;
    }

    G2Point plus(G2Point Point) {
        return new G2Point(x + Point.x, y + Point.y);
    }

    G2Point minus(G2Point Point) {
        return new G2Point(x - Point.x, y - Point.y);
    }

    G2Point times(double Value) {
        return new G2Point(x * Value, y * Value);
    }
}