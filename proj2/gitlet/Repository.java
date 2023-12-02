package gitlet;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import java.nio.channels.NotYetBoundException;
import java.nio.file.FileAlreadyExistsException;
import java.util.*;
import java.util.zip.CheckedInputStream;


import static gitlet.Utils.*;
import static gitlet.Utils.readContentsAsString;


/**
 * Represents a gitlet repository.
 * <p>
 * does at a high level.
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
    public static final File SPLITHASH_MAP = join(GITLET_DIR, "splitHash_map");
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

        StagingArea.StagingRepo.mkdir();
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
        //相同
        if (curfile != null && fileEquals(target, curfile)) {
            staging.DeleteFromAddtion(filename);
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

        staging.clean();
        head.setCommit(newcommit);
        head.toFile();
    }

    void commit(String message, Commit secPar) {//
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
        Commit newcommit = new Commit(message, head.getCommit(), secPar, newSnap);
        newcommit.toFile();

        staging.clean();
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

        if (head.getCommit().snap.containsKey(cwd_file_name)) {
            File f = join(cwd_file_name);
            if (f.exists()) {
                restrictedDelete(f);
            }
            staging.AddToremoval(cwd_file_name);// 我居然忘了加removal？？？ 前提条件：在cmisnap
        }
        head.getCommit().snap.remove(cwd_file_name);
        staging.DeleteFromAddtion(cwd_file_name);
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
            Set<String> trackedFileName = trackingFileName();
            for (String commFileName : nextCommit.snap.keySet()) {
                File cwdFile = join(commFileName);
                if (!trackedFileName.contains(commFileName) && cwdFile.exists()) {
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
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
            File targetFile = curCommit.GetFile(targetName);//如果文件不在commit中，返回的应该是null，而不是一个引向某个文件的file
            if (targetFile == null || !targetFile.exists()) {
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
        assert giverCommit.GetFile(targetName) != null;
        File cwdF = join(targetName);
        if (!cwdF.exists()) {
            try {
                cwdF.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        loadFile(giverCommit.GetFile(targetName), join(targetName));
    }

    void check(Branch br) {
        Commit nextCommit = br.getCommit();
        resetFile(nextCommit);
        head = br;
        staging.clean();
    }

    void resetFile(Commit nextCommit) {
        Set<String> untrackedName = trackingFileName();
        for (String fName : nextCommit.snap.keySet()) {
            loadFile(nextCommit.snap.get(fName), join(fName));
            untrackedName.remove(fName);
        }
        for (String delFName : untrackedName) {
            restrictedDelete(delFName);
        }
    }

    boolean findAndCheck(String mes) {
        boolean valid = false;
        for (String commitID : Objects.requireNonNull(plainFilenamesIn(Commit.COMMITS_REPO))) {
            Commit c = Commit.Getcommit(commitID);
            assert c != null;
            if (c.message.equals(mes)) {
                System.out.println(c.GetHash());
                valid = true;
            }
        }
        return valid;
    }

    void showStatus() {
        System.out.println("=== Branches ===");
        for (String brName : Objects.requireNonNull(plainFilenamesIn(Branch.BRANCHES_REPO))) {
            if (head.equals(Branch.GetBranch(brName))) {
                System.out.print("*");
            }
            System.out.println(brName);
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String fName : staging.additon) {
            System.out.println(fName);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String fName : staging.removal) {
            System.out.println(fName);
        }
        System.out.println();


        /*
        commitsnap,staging,cwd，cwdcopy
        遍历tracking，先根据commitsnap来判断，若其文件不在cmi，则看staging
        遍历commitsnap,
            看cwd文件是否存在，
                若存在。若snap与cwd的文件不同，且不在staging，为mo。
                若不存在，removal不存在，为mo
             删除cwdcopy元素
         遍历staging
            若其文件与cwd文件不同，或cwd文件不存在
            删除cwdcopy元素
         */


        System.out.println("=== Modifications Not Staged For Commit ==="); // 碰到conflict的文件如何处理？
        List<String> cwdcopy = new ArrayList<>(Objects.requireNonNull(plainFilenamesIn(CWD)));// 引用的问题，返回的居然是一个array！
        Set<String> tracked = trackingFileName();
        Commit curCmi = head.getCommit();
        for (String fn : tracked) {
            File cwdF = join(fn);
            File cmiF = curCmi.GetFile(fn);
            File additionF = staging.getFile(fn);


            cwdcopy.remove(fn);
            if (cwdF.exists() && isConflictFile(cwdF)) {
                continue;
            }
            if (cmiF != null) {
                if (cwdF.exists()) {
                    if (!fileEquals(cwdF, cmiF) && additionF == null) {
                        System.out.printf("%s (modified)\n", fn);
                    }
                } else if (!staging.ExistInremoval(fn)) {
                    System.out.printf("%s (deleted)\n", fn);
                }
            } else if (additionF != null) {
                if (cwdF.exists()) {
                    if (!fileEquals(additionF, cwdF)) {
                        System.out.printf("%s (modified)\n", fn);
                    }
                } else {
                    System.out.printf("%s (deleted)\n", fn);
                }
            } else {
                throw new RuntimeException("status werid");
            }
        }
        System.out.println();

        System.out.println("=== Untracked Files ===");
        assert cwdcopy != null;
        for (String fn : cwdcopy) {
            System.out.println(fn);
        }
        System.out.println();

    }

    private boolean isConflictFile(File f) {
        String content = readContentsAsString(f);
        return content.startsWith("<<<<<<< HEAD") && content.endsWith(">>>>>>>");
    }

    private boolean fileEquals(File f1, File f2) {
        return fileHash(f1).equals(fileHash(f2));
    }

    void branchCheck(String brName) {
        File f = join(Branch.BRANCHES_REPO, brName);
        if (f.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
    }

    void branch(String brName) {
        Branch br = new Branch(head.getCommit().GetHash(), brName);
        br.toFile();
        splitMapAdd(br);
    }

    void rmBranchCheck(String brName) {
        File f = join(Branch.BRANCHES_REPO, brName);
        if (!f.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (brName.equals(head.getName())) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
    }

    void rmBranch(String brName) {
        Branch.removeBranch(brName);
        splitMapRemove(brName);
    }

    private void splitMapRemove(String brName) {
        HashMap<Set<String>, String> newsm = new HashMap<>();

        @SuppressWarnings("unchecked")
        HashMap<Set<String>, String> splitmap = readObject(SPLITHASH_MAP, HashMap.class);

        for (Set<String> s : splitmap.keySet()) {
            if (!s.contains(brName)) {
                newsm.put(s, splitmap.get(s));
            }
        }
        writeObject(SPLITHASH_MAP, newsm);
    }


    @SuppressWarnings("unchecked")
    private void splitMapAdd(Branch newB) {
        File f = SPLITHASH_MAP;
        HashMap<Set<String>, String> splitMap;
        HashMap<Set<String>, String> oldsmp;
        if (f.exists()) {
            oldsmp = readObject(f, HashMap.class);
            splitMap = new HashMap<Set<String>, String>(oldsmp);
        } else {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("splitMapFile fail to create");
            }
            splitMap = new HashMap<>();
            oldsmp = new HashMap<>();
        }

        Set<String> brPair = new HashSet<>();
        brPair.add(head.getName());
        brPair.add(newB.getName());
        splitMap.put(brPair, head.getCommit().GetHash());
        for (Set<String> s : oldsmp.keySet()) {// head,A -> commitHash1 , newB,A -> commitHash1
            if (s.contains(head.getName())) {
                brPair = new HashSet<>(s);
                brPair.remove(head.getName());
                brPair.add(newB.getName());
                splitMap.put(brPair, splitMap.get(s));
            }
        }
        writeObject(SPLITHASH_MAP, splitMap);
    }

    void resetCheck(String cmiHash) {
        File f = join(Commit.COMMITS_REPO, getFullHash(cmiHash));
        if (!f.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit nextCmi = Commit.Getcommit(getFullHash(cmiHash));
        assert nextCmi != null;
        Set<String> trackedFileName = trackingFileName();//
        for (String fName : nextCmi.snap.keySet()) {
            File cwdFile = join(fName);
            if (!trackedFileName.contains(fName) && cwdFile.exists()) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    void reset(String cmiHash) {
        Commit nextCmi = Commit.Getcommit(getFullHash(cmiHash));
        resetFile(Objects.requireNonNull(nextCmi));
        head.setCommit(nextCmi);
        head.toFile();
        staging.clean();
    }

    void mergeCheck(String brName) {
        boolean valid = true;
        if (!isNoChange()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        File brf = join(Branch.BRANCHES_REPO, brName);
        if (!brf.exists()) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
            //后面还会往下继续计算
        }
        if (brName.equals(head.getName())) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        Set<String> trackedFileName = trackingFileName();
        Commit giverCmi = Branch.GetBranch(brName).getCommit();
        for (String fileCmi : giverCmi.snap.keySet()) {
            File cwdF = join(fileCmi);
            if (!trackedFileName.contains(fileCmi) && cwdF.exists()) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first");
                System.exit(0);
            }
        }
        //这种写法很糟糕
//        if (!valid) {
//            System.exit(0);
//        }
    }

    void merge(String brName) {
        /*
        寻找split point点，更新(head,br)的split->merged
        遍历head的tracking file
            每个file都有三个状态,都是根据split的file确定，是否modified，是否为new，是否存在
         */
        boolean isConflict = false;
        Branch mergedBr = Branch.GetBranch(brName);
        Commit splitCmi = GetSplitCmi(head, mergedBr);

        @SuppressWarnings("unchecked")
        HashMap<Set<String>, String> splitMap = readObject(SPLITHASH_MAP, HashMap.class);
        Set<String> s = new TreeSet<>();
        s.add(head.getName());
        s.add(brName);
        splitMap.put(s, mergedBr.getCommit().GetHash());
        writeObject(SPLITHASH_MAP, splitMap);

        Commit receiver = head.getCommit();
        Commit giver = mergedBr.getCommit();
        Set<String> giverRemain = new TreeSet<>(giver.snap.keySet());// 代码实现问题，返回一个object副本，而不是引用


        //不仅写反了，甚至条件都写错了
        if (giver.equals(splitCmi)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (receiver.equals(splitCmi)) {
            System.out.println("Current branch fast-forwarded.");
            check(brName);
            return;
        }
        for (String receiverFn : receiver.snap.keySet()) {
            giverRemain.remove(receiverFn);

            File giverF = giver.GetFile(receiverFn);
            File splitF = splitCmi.GetFile(receiverFn);
            File receiveF = receiver.GetFile(receiverFn);
            if (splitF == null) {
                if (giverF != null && !fileEquals(receiveF, giverF)) {
                    conflictFix(receiveF, giverF, receiverFn);
                    isConflict = true;
                }
            } else {
                if (splitF.equals(receiveF)) {// A A
                    if (giverF == null) {
                        rm(receiverFn);
                    } else if (!fileEquals(splitF, giverF)) {
                        check(giver.GetHash(), receiverFn);
                        add(receiverFn);
                    }
                } else { // A !A
                    if (giverF == null || !fileEquals(giverF, splitF)) {
                        conflictFix(receiveF, giverF, receiverFn);
                        isConflict = true;
                    }
                }
            }
        }
        for (String giverFn : giverRemain) {
            File giverF = giver.GetFile(giverFn);
            File splitF = splitCmi.GetFile(giverFn);
            File receiveF = receiver.GetFile(giverFn);
            if (splitF == null) { // x x
                loadFile(giverF, join(giverFn));//staging 没有处理
                add(giverFn);
            } else {// A x
                if (!fileEquals(giverF, splitF)) {
                    conflictFix(receiveF, giverF, giverFn);
                    isConflict = true;
                }
            }
        }

        commit("Merged [" + brName + "] into [" + head.getName() + "].", giver);//注意这个commit("s")是无merge的情况;多打了换行符
        if (isConflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    private void conflictFix(File headF, File mergedF, String fileName) {// 由commit返回的file对象！
        String contentHead = "", contentMerged = "";
        if (headF != null) {
            contentHead = readContentsAsString(headF);
        }
        if (mergedF != null) {
            contentMerged = readContentsAsString(mergedF);
        }
        File cwdF = join(fileName);
        if (!cwdF.exists()) {
            try {
                cwdF.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        writeContents(cwdF, "<<<<<<< HEAD\n", contentHead, "\n", "=======\n", contentMerged, "\n>>>>>>>");
    }

    Commit GetSplitCmi(Branch a1, Branch a2) {
        @SuppressWarnings("unchecked")
        HashMap<Set<String>, String> smp = readObject(SPLITHASH_MAP, HashMap.class);

        Set<String> S = new HashSet<>();
        S.add(a1.getName());
        S.add(a2.getName());
        return Commit.Getcommit(smp.get(S));
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
