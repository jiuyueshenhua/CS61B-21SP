package gitlet;

import static gitlet.Utils.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author TODO
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {


        System.out.println("sss"+"xxxx");
        CheckArgsBefore(args);
        String firstArg = args[0];

        File f = Repository.CURREPO;
        Repository CurRepo;
        if (f.exists()) {
            CurRepo = readObject(f, Repository.class);
        } else {
            CurRepo = new Repository();
        }

        switch (firstArg) {
            case "init":
                if (Repository.GITLET_DIR.exists()) {
                    System.out.println("A Gitlet version-control system already exists in the current directory.");
                    System.exit(0);
                } else {
                    CurRepo.init();
                }
                break;
            case "add":
                File target = join(Repository.CWD, args[1]);
                if (!target.exists()) {
                    System.out.println("File does not exist.");
                    System.exit(0);
                }
                CurRepo.add(args[1]);
                break;
            case "commit":
                if (CurRepo.isNoChange()) {
                    System.out.println("No changes added to the commit.");
                    System.exit(0);
                }
                if (args[1].isEmpty()) {
                    System.out.println("Please enter a commit message");
                    System.exit(0);
                }
                CurRepo.commit(args[1]);
                break;
            case "rm":
                CurRepo.rmCheck(args[1]);
                CurRepo.rm(args[1]);
                break;
            case "log":
                CurRepo.log();
                break;
            case "global-log":
                CurRepo.global_log();
                break;
            case "checkout":
                CurRepo.checkoutCheck(args);
                if (args.length == 2) {
                    CurRepo.check(Branch.GetBranch(args[1]));
                }
                if (args.length == 4) {
                    CurRepo.check(args[1], args[3]);// checkout [commit id] -- [file name]
                }
                if (args.length == 3) {
                    CurRepo.check(args[2]);
                }
                break;
            case "find":
                if (!CurRepo.findAndCheck(args[1])) {
                    System.out.println("Found no commit with that message.");
                    System.exit(0);
                }
                break;
            case "status":
                CurRepo.showStatus();
                break;
            case "branch":
                CurRepo.branchCheck(args[1]);
                CurRepo.branch(args[1]);
                break;
            case "rm-branch":
                CurRepo.rmBranchCheck(args[1]);
                CurRepo.rmBranch(args[1]);
                break;
            case "reset":
                CurRepo.resetCheck(args[1]);
                CurRepo.reset(args[1]);
                break;
            case "merge":
                CurRepo.mergeCheck(args[1]);
                CurRepo.merge(args[1]);
                break;
        }

        CurRepo.toFile();
    }

    private static void sertest() {
        //System.out.println(Commit.CreateInitCommit().GetHash());
        Map<Set<String>, String> m = new HashMap<>();
        Set<String> s = new HashSet<>();
        s.add("aa");
        s.add("bb");
        m.put(s, "ab");

        s = new HashSet<>();
        s.add("cc");
        s.add("dd");
        m.put(s, "cd");

        File f = join("testmap");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        writeObject(f, (Serializable) m);
        System.out.println(m);
        System.out.println("----");

    }

    private static void desertest() {

        @SuppressWarnings("unchecked")
        HashMap<Set<String>, String> m = readObject(join("testmap"), HashMap.class);
        System.out.println(m);
        for (Set<String> s : m.keySet()) {
            System.out.print(s + "  ");
            System.out.println(m.get(s));
        }
    }

    private static void CheckArgsBefore(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command");
            System.exit(0);
        }
        CheckValidFormat(args);
        if (!args[0].equals("init")) {
            CheckIsInited();
        }
    }

    private static void CheckIsInited() {
        File f = join(System.getProperty("user.dir"), ".gitlet");
        if (!f.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    private static void CheckValidFormat(String[] args) {


        Map<String, Integer> num = new TreeMap<>();
        num.put("init", 1);
        num.put("add", 2);
        num.put("commit", 2);
        num.put("rm", 2);
        num.put("log", 1);
        num.put("global-log", 1);
        num.put("find", 2);
        num.put("status", 1);
        num.put("branch", 2);
        num.put("rm-branch", 2);
        num.put("reset", 2);
        num.put("merge", 2);

        if (args[0].equals("checkout")) {
            boolean valid = true;
            if (args.length == 3) {
                valid = args[1].equals("--");
            } else if (args.length == 4) {
                valid = args[2].equals("--");
            } else {
                valid = args.length == 2;
            }
            if (!valid) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        } else {
            if (!num.containsKey(args[0])) {
                System.out.println("No command with that name exists.");
                System.exit(0);
            }
            if (args.length != num.get(args[0])) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
        }
    }
}
