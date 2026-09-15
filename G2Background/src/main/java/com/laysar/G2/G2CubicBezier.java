package com.laysar.G2;

final class G2CubicBezier {
    final G2Point p0;
    final G2Point p1;
    final G2Point p2;
    final G2Point p3;

    G2CubicBezier(G2Point P0, G2Point P1, G2Point P2, G2Point P3) {
        p0 = P0;
        p1 = P1;
        p2 = P2;
        p3 = P3;
    }

    G2CubicBezier times(double Value) {
        return new G2CubicBezier(p0.times(Value), p1.times(Value), p2.times(Value), p3.times(Value));
    }
}