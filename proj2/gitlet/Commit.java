package gitlet;


import java.io.*;

import java.text.SimpleDateFormat;
import java.util.*;

import static  gitlet.Utils.*;

/**
 * Represents a gitlet commit object.
 * <p>
 * does at a high level.
 *
 * @author TODO
 */
public class Commit implements Serializable {

    /**
     * The message of this Commit.
     */
    public static final File COMMITS_REPO = Utils.join(Repository.GITLET_DIR, "Commits");
     String message;
     String firParentHash, secParentHash;//commit parent hashcode
     Map<String, File> snap;//FileName -> Blob
     Date date;

    Commit(String M, Commit firParent, Map<String, File> snap) {
        date = new Date();
        message = M;
        this.firParentHash = firParent.GetHash();
        this.snap = snap;
    }

    Commit(String M, Commit firParent, Commit secParent, Map<String, File> snap) {
        this(M, firParent, snap);
        this.secParentHash = secParent.GetHash();
    }
    Commit(Date d) {
        this.date=d;
        snap=new TreeMap<>();
    }

    /*
    commit的相关操作
     */
    boolean isMerged() {
        return firParentHash != null && secParentHash != null;
    }

    String GetHash() {
        return sha1(message+date.toString()+firParentHash);
    }

    boolean IsChildFor(Commit par) {
        return IsChildHelper(this,par);
    }

    /**
     * 当cur和giver相同时，返回true
     * cur是giver的child时，返回true
     * @param cur
     * @param giver
     * @return
     */
    private boolean IsChildHelper(Commit cur,Commit giver) {
           if(cur.equals(giver)) {
               return true;
           }
           if(cur.isInitCommit()) {
               return false;
           }
           if(cur.isMerged()) {
               return IsChildHelper(cur.GetFirParent(),giver) || IsChildHelper(cur.GetSecParent(),giver);
           }
           return IsChildHelper(cur.GetFirParent(),giver);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj==null) return false;
        if(obj instanceof Commit ) {
            return this.GetHash().equals(((Commit) obj).GetHash());
        }
        return false;
    }

    Commit GetFirParent() {
        return Getcommit(firParentHash);
    }
    boolean isInitCommit() {
        return this.GetHash().equals(Commit.CreateInitCommit().GetHash());
    }


    Commit GetSecParent() {
        return Getcommit(secParentHash);
    }

    /**
     *
     * @param filename
     * @return
     * 无相应文件时返回null
     */
    File GetFile(String filename) {
        return snap.get(filename);
    }

    void display() {
        System.out.println("===");
        System.out.printf("commit %s\n",GetHash());
        if(isMerged()) {
            System.out.printf("Merge: %s %s\n",firParentHash.substring(0,7),secParentHash.substring(0,7));
        }

        SimpleDateFormat sdf = new SimpleDateFormat("E MMM d HH:mm:ss yyyy Z", Locale.ENGLISH);
        String formattedDate = sdf.format(this.date);
        System.out.println("Date: " + formattedDate);
        System.out.println(this.message);
        System.out.println();
    }



    /*
    与IO相关的操作
     */
    void toFile() {
        File f = Utils.join(COMMITS_REPO,GetHash());
        if(!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Utils.writeObject(f,this);
    }

    /*
    全局操作
     */
    static Commit CreateInitCommit() {
        Commit c = new Commit(new Date(0));
        c.message="initial commit";
        return c;
    }

    static Commit Getcommit(String hash) {//从文件名寻找
        File f = join(COMMITS_REPO,hash);
        if(!f.exists()) {
            return null;
        }
        return readObject(f,Commit.class);
    }
}
