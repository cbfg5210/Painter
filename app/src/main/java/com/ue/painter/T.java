package com.ue.painter;

import java.io.File;
import java.util.Arrays;

/**
 * Created by hawk on 2018/1/25.
 */

public class T {
    private void a(File f) {
        File[] files = f.listFiles();
        Arrays.sort(files, (o1, o2) -> o1.lastModified() == o2.lastModified() ? -1 : (o1.lastModified() < o2.lastModified() ? 1 : 0));

//        Arrays.sort(files, new Comparator() {
//            public int compare(Object o1, Object o2) {
//
//                if (((File) o1).lastModified() > ((File) o2).lastModified()) {
//                    return -1;
//                } else if (((File) o1).lastModified() < ((File) o2).lastModified()) {
//                    return +1;
//                } else {
//                    return 0;
//                }
//            }
//
//        });
    }
}
