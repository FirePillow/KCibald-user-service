package com.kcibald.services.user;

import java.util.BitSet;

public class JHelper {
    public static BitSet makeUrlDontNeedEncodingBitSet() {
//        from java.net.URLEncoder
        var dontNeedEncoding = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = '0'; i <= '9'; i++) {
            dontNeedEncoding.set(i);
        }
        dontNeedEncoding.set('-');
        dontNeedEncoding.set('_');
        dontNeedEncoding.set('.');
        dontNeedEncoding.set('*');
        return dontNeedEncoding;
    }
}
