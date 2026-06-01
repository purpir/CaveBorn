package ru.purpir.client;

public class SolarPointsClientState {
    private static int points;

    public static int getPoints() {
        return points;
    }

    public static void setPoints(int points) {
        SolarPointsClientState.points = Math.max(0, Math.min(100, points));
    }
}
