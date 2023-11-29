package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.StreamSupport;

import static gitlet.Utils.*;
import static gitlet.Utils.readContentsAsString;


/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository implements Serializable {
    /**
     *
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File SPLIT_MAP = join(GITLET_DIR, "split_map");
    public static final File BLOBS_REPO = join(GITLET_DIR, "Blobs");
    public static final File CURREPO = join(GITLET_DIR, "CurRepo");


    /*
    TODO: 文件结构：
    .gitlet
        Blobs
        Branches
        commits
        repository
        split_Map
     */

    private Branch head;//todo:private
    private StagingArea staging;

    void init() {
        GITLET_DIR.mkdir();
        BLOBS_REPO.mkdir();
        SPLIT_MAP.mkdir();
        Commit.COMMITS_REPO.mkdir();
        Branch.BRANCHES_REPO.mkdir();
        Commit initcommit = Commit.CreateInitCommit();
        Branch master = new Branch(initcommit.GetHash(), "master");
        head = master;
        staging = new StagingArea();

        master.toFile();
        initcommit.toFile();

    }

    void add(String filename) {
        File target = join(Repository.CWD, filename);
        File curfile = head.getCommit().GetFile(filename);
        staging.DeleteFromRemocal(filename);
        if (curfile != null && sha1(readContentsAsString(curfile)).equals(readContentsAsString(target))) {
            return;
        }
        staging.AddToaddition(filename);
    }

    void commit(String message) {//
        /*
        CWD: text.txt
        Blobs:
            aasdf
            dfada
        作新的snap：
            复制head commit的snap
            根据removal，取消tracked文件
            根据addition名单，把cwd的文件复制到blobs，并以其sha1命名。并将该映射添加到newsnap里
         */
        HashMap<String, File> newSnap = new HashMap<>(head.getCommit().snap);

        for (String cwd_file_name : staging.removal) {
            newSnap.remove(cwd_file_name);
        }
        for (String cwd_file_name : staging.additon) {
            saveToBlobs(cwd_file_name);
            newSnap.put(cwd_file_name, join(BLOBS_REPO, fileHash(join(cwd_file_name))));
        }
        Commit newcommit = new Commit(message, head.getCommit(), newSnap);
        newcommit.toFile();
        head.setCommit(newcommit);
        head.toFile();
    }

    void rmCheck(String cwd_file_name) {//检查，若错误，退出程序
        if (!staging.additon.contains(cwd_file_name) && !head.getCommit().snap.containsKey(cwd_file_name)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    void rm(String cwd_file_name) {
        staging.additon.remove(cwd_file_name);
        head.getCommit().snap.remove(cwd_file_name);
        File f = join(cwd_file_name);
        if (f.exists()) {
            restrictedDelete(f);
        }
    }

    void log() {
        logHelper(head.getCommit());
    }

    void global_log() {
        List<String> files_name = plainFilenamesIn(Commit.COMMITS_REPO);
        assert files_name != null;
        for (String comfile_name : files_name) {
            File f = join(Commit.COMMITS_REPO, comfile_name);
            readObject(f, Commit.class).display();
        }
    }

    private void logHelper(Commit c) {
        c.display();
        if (c.isInitCommit()) {
            return;
        }
        logHelper(c.GetFirParent());
    }

    void checkoutCheck(String[] args) {
        if (args.length == 2) { //  checkout [branch name]
            File br_file = join(Branch.BRANCHES_REPO, args[1]);

            if (!br_file.exists()) {
                System.out.println("No such branch exists");
                System.exit(0);
            }
            Branch br = readObject(br_file, Branch.class);
            if (head.equals(br)) {
                System.out.println("No need to checkout the current branch.");
                System.exit(0);
            }
            Commit nextCommit = br.getCommit();
            for (String commFileName : nextCommit.snap.keySet()) {
                File cwdFile = join(commFileName);
                if (!cwdFile.exists()) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
            }

        } else {
            if (args.length == 4) {// checkout [commit id] -- [file name]
                File com = join(Commit.COMMITS_REPO, getFullHash(args[1]));
                if (!com.exists()) {
                    System.out.println("No commit with that id exists.");
                    System.exit(0);
                }
            }
            String targetName;
            Commit curCommit;
            if (args.length == 4) {
                targetName = args[3];
                curCommit = Commit.Getcommit(getFullHash(args[1]));
            } else { // checkout -- [file name]
                targetName = args[2];
                curCommit = head.getCommit();
            }
            assert curCommit != null;
            File targetFile = curCommit.GetFile(targetName);
            if (!targetFile.exists()) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
        }
    }

    void check(String targetName) {
        Commit curcommit = head.getCommit();
        File trackedFile = curcommit.snap.get(targetName);
        loadFile(trackedFile, join(targetName));
    }

    void check(String commitID, String targetName) {
        Commit giverCommit = Commit.Getcommit(getFullHash(commitID));
        assert giverCommit != null;
        loadFile(giverCommit.GetFile(targetName), join(targetName));
    }

    void check(Branch br) {
        Commit nextCommit = br.getCommit();
        Set<String> untrackedName = trackingFileName();
        for (String fName:nextCommit.snap.keySet()) {
            loadFile(nextCommit.snap.get(fName),join(fName));
            untrackedName.remove(fName);
        }
        for(String delFName:untrackedName) {
            restrictedDelete(delFName);
        }
        head = br;
        staging.clean();
    }


    void toFile() {
        File f = Repository.CURREPO;
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeObject(f, this);
    }

    boolean isNoChange() {
        return staging.additon.isEmpty() && staging.removal.isEmpty();
    }

    private void saveToBlobs(String fileName) {
        File CWD_file = join(CWD, fileName);
        File git_file = join(BLOBS_REPO, fileHash(CWD_file));
        if (!git_file.exists()) {
            try {
                git_file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeContents(git_file, readContentsAsString(CWD_file));
    }

    static String fileHash(File f) {// 对文件内容进行哈希
        return sha1(readContentsAsString(f));
    }

    static void loadFile(File giver, File receiver) {
        if (!receiver.exists()) {
            try {
                receiver.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeContents(receiver, readContentsAsString(giver));
    }


    Set<String> trackingFileName() {
        Set<String> s = new TreeSet<>(staging.additon);
        s.addAll(head.getCommit().snap.keySet());
        return s;
    }

    static String getFullHash(String arrive) {
        if (arrive.length() == UID_LENGTH) {
            return arrive;
        }
        for (String comFile : Objects.requireNonNull(plainFilenamesIn(Commit.COMMITS_REPO))) {
            if (comFile.startsWith(arrive)) {
                return comFile;
            }
        }
        return null;
    }
    void display() {
        System.out.printf("head: %s\n", head.getName());
        System.out.printf("staging: %s %s\n", staging.additon.toString(), staging.removal.toString());
    }
}
