package gitlet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class StagingArea implements Serializable {
    Set<String> additon, removal;//string为文件名字


    StagingArea() {
        additon = new TreeSet<>();
        removal = new TreeSet<>();
    }

    /*
    这里的函数封装过度了。因为addition和removal都是可访问的。
     */
    boolean ExistInaddition(String s) {
        return additon.contains(s);
    }

    boolean ExistInremoval(String s) {
        return removal.contains(s);
    }

    boolean AddToaddition(String s) {
        return additon.add(s);
    }

    boolean AddToremoval(String s) {
        return removal.add(s);
    }

    boolean DeleteFromAddtion(String s) {
        return additon.remove(s);
    }

    boolean DeleteFromRemocal(String s) {
        return removal.remove(s);
    }

    void clean() {
        additon.clear();
        removal.clear();
    }
}
