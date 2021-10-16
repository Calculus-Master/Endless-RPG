package com.calculusmaster.endlessrpg.util.helpers;

import java.util.SplittableRandom;

public class IDHelper
{
    private static final String source = "abcdefghijklmnoqrstuvwxyz0123456789";

    public static String create(int length)
    {
        final SplittableRandom r = new SplittableRandom();
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < length; i++) s.append(source.charAt(r.nextInt(source.length())));
        return s.toString();
    }
}
