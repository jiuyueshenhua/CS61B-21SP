package gitlet;

import javax.print.attribute.standard.ReferenceUriSchemesSupported;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Set;
import static gitlet.Utils.*;

public class Branch implements Serializable {
    private String curCommitHash;
    private String name;
    public static final File BRANCHES_REPO = Utils.join(Repository.GITLET_DIR, "Branches");

    public Branch(String hash, String na) {
        curCommitHash = hash;
        name = na;
    }


    Commit getCommit() {
        return Commit.Getcommit(curCommitHash);
    }

    void setCommit(Commit commit) {
        this.curCommitHash=commit.GetHash();
    }


    String getName() {
        return name;
    }


    static List<String> getBranches() {// log,branch,会用到
        return plainFilenamesIn(BRANCHES_REPO);
    }

    void toFile() {
        File f = Utils.join(BRANCHES_REPO,this.name);
        if (!f.exists()) {
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Utils.writeObject(f,this);
    }
    @Override
    public boolean equals(Object o){
        if( o == null ) return false;
        if(o instanceof Branch) {
            return ((Branch) o).name.equals(this.name);
        }
        return false;
    }
    static Branch GetBranch(String brName) {
        File f = join(BRANCHES_REPO,brName);
        if(!f.exists()) {
            throw new RuntimeException("no branch filename");
        }
        return readObject(f,Branch.class);
    }
    static void removeBranch(String brName) {
        File f=join(BRANCHES_REPO,brName);
        restrictedDelete(f);
    }
}
