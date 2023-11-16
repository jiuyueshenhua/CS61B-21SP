package bstmap;


import java.util.*;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {

    private class Node {
        K key;
        V value;
        Node left; // <key的树
        Node right; // > key 的树

        public Node(K key, V val) {
            this.key = key;
            this.value = val;
            this.left = null;
            this.right = null;
        }
    }

    private Node root;
    private int nodeNum;

    @Override
    public void clear() {
        root = null;
        nodeNum = 0;
    }

    @Override
    public int size() {
        return nodeNum;
    }

    @Override
    public void put(K key, V value) {
        nodeNum++;
        root = putHelper(this.root, key, value);
    }

    private Node putHelper(Node cur, K key, V value) {
        if (cur == null) {
            return new Node(key, value);
        }
        if (cur.key.equals(key)) {
            cur.value = value;
        } else if (key.compareTo(cur.key) > 0) {
            cur.right = putHelper(cur.right, key, value);
        } else {
            cur.left = putHelper(cur.left, key, value);
        }
        return cur;
    }

    @Override
    public V get(K key) {
        return getHelper(root, key);
    }

    private V getHelper(Node cur, K key) {
        if (cur == null) {
            return null;
        }
        if (cur.key.equals(key)) {
            return cur.value;
        } else if (key.compareTo(cur.key) > 0) {
            return getHelper(cur.right, key);
        } else {
            return getHelper(cur.left, key);
        }
    }

    @Override
    public boolean containsKey(K key) {
        return containHelper(root, key);
    }

    private boolean containHelper(Node cur, K key) {
        if (cur == null) {
            return false;
        }
        if (cur.key.equals(key)) {
            return true;
        } else if (key.compareTo(cur.key) > 0) {
            return containHelper(cur.right, key);
        } else {
            return containHelper(cur.left, key);
        }
    }

    public void printInOrder() {
        System.out.println("Key" + "\t" + "Val");
        printHelper(root);
        System.out.println();
    }

    private void printHelper(Node cur) {
        if (cur == null) {
            return;
        }
        printHelper(cur.left);
        System.out.println(cur.key + "\t" + cur.value);
        printHelper(cur.right);
    }

    @Override
    public Set<K> keySet() {
        Set<K> S = new HashSet<>();
        Queue<Node> Q = new ArrayDeque<>();
        int num = 0;
        if (root != null) {
            Q.add(root);
            num++;
        }
        while (!Q.isEmpty()) {
            int nextnum = 0;
            for (int i = 0; i < num; i++) {
                Node cur = Q.poll();
                S.add(cur.key);
                //System.out.print(cur.key + " ");
                if (cur.right != null) {
                    Q.add(cur.right);
                    nextnum++;
                }
                if (cur.left != null) {
                    Q.add(cur.left);
                    nextnum++;
                }
            }
            //System.out.println();
            num = nextnum;
        }
        return S;
    }

    private void forinOrder() {
        Stack<Node> s = new Stack<>();
        Node cur = root;

        while (!s.isEmpty() || cur != null) {
            while (cur != null) {
                s.push(cur);
                cur = cur.left;
            }
            cur = s.pop();
            System.out.println(cur.key);
            cur = cur.right;
        }
    }

    @Override
    public V remove(K key) {
        V ans = get(key);
        if (ans != null) {
            root = removeKeyHelper(root, key);
        }
        return ans;
    }

    private Node removeKeyHelper(Node cur, K key) {
        if (cur == null) {
            return null;
        }
        if (cur.key.equals(key)) {
            if (cur.left == null && cur.right == null) {
                return null;
            }
            if (cur.left != null && cur.right != null) {

                Node now = cur.left;

                while (now.right != null) {
                    now = now.right;
                }
                Node maxleft = new Node(now.key, now.value);
                cur.left = removeKeyHelper(cur.left, maxleft.key);
                cur.key = maxleft.key;
                cur.value = maxleft.value;
                return cur;
            }
            if (cur.left != null) {
                return cur.left;
            } else {
                return cur.right;
            }
        }
        if (key.compareTo(cur.key) > 0) {
            cur.right = removeKeyHelper(cur.right, key);
        } else {
            cur.left = removeKeyHelper(cur.left, key);
        }
        return cur;
    }

    @Override
    public V remove(K key, V value) {
        V ans = get(key);
        if (ans != null && ans == value) {
            root = removeKeyHelper(root, key);
        } else {
            ans = null;
        }
        return ans;
    }


    @Override
    public Iterator<K> iterator() {
        return keySet().iterator();
    }

    public static void main(String[] args) {

    }
}
