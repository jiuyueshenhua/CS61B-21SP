package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import java.util.Set;
import java.util.TreeSet;

import static gitlet.Utils.*;

public class StagingArea implements Serializable {
    Set<String> additon, removal;//string为文件名字
    public static final File StagingRepo = join(Repository.GITLET_DIR, "Staging");
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
        File cwd = join(Repository.CWD,s);
        File newf = join(StagingRepo,s);
        if(!newf.exists()) {
            try {
                newf.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeContents(newf,readContentsAsString(join(cwd)));
        return additon.add(s);
    }

    boolean AddToremoval(String s) {
        return removal.add(s);
    }

    boolean DeleteFromAddtion(String s) {
        File f = join(StagingRepo,s);
        f.delete();
        return additon.remove(s);
    }

    boolean DeleteFromRemocal(String s) {
        return removal.remove(s);
    }

    void clean() {
        for(String fn:additon) {
            File f = join(StagingRepo,fn);
            f.delete();
        }
        additon.clear();
        removal.clear();
    }

    /**
     *
     * @param Name
     * @return 文件不存在时返回null
     */
    File getFile(String Name) {
        if(!additon.contains(Name)) {
            return null;
        }
        return join(StagingRepo,Name);
    }
}
