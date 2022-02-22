package net.azisaba.velocityCommands.util;

import java.util.List;
import java.util.Objects;

public class Util {
    public static <T> int count(List<T> list, T value) {
        int i = 0;
        for (T t : list) {
            if (t == value) i++;
        }
        return i;
    }

    public static <T> int countEquals(List<T> list, T value) {
        int i = 0;
        for (T t : list) {
            if (Objects.equals(t, value)) i++;
        }
        return i;
    }
}
