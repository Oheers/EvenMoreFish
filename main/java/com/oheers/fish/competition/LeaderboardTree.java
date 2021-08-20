package com.oheers.fish.competition;

import com.oheers.fish.fishing.items.Fish;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LeaderboardTree {

    Node root;
    HashMap<Player, Node> playerRegister = new HashMap<>();
    List<Node> topEntrants = new ArrayList<>();

    private boolean shouldAdd(Player fisher, Float length) {
        if (!playerRegister.containsKey(fisher)) {
            return true;
        } else {
            if (playerRegister.get(fisher).getLength() < length) {
                deleteOldScore(playerRegister.get(fisher));
                return true;
            } else return false;
        }
    }

    private void addToRegister(Player fisher, Node node) {
        playerRegister.put(fisher, node);
    }

    public void addNode(Fish fish, Player fisher) {
        if (shouldAdd(fisher, fish.getLength())) {
            Node newNode = new Node(fish.getLength(), fish, fisher);

            if (root == null) {
                root = newNode;
                addToRegister(fisher, root);
            }
            else {
                Node focusNode = root;
                Node parentNode;

                while(true) {
                    parentNode = focusNode;
                    if (newNode.length < focusNode.length) {

                        focusNode = focusNode.leftChild;
                        if (focusNode == null) {
                            parentNode.leftChild = newNode;
                            newNode.parent = parentNode;
                            newNode.isLeft = true;
                            addToRegister(fisher, newNode);
                            return;
                        }
                    } else {
                        focusNode = focusNode.rightChild;

                        if (focusNode == null) {
                            parentNode.rightChild = newNode;
                            newNode.parent = parentNode;
                            newNode.isLeft = false;
                            addToRegister(fisher, newNode);
                            return;
                        }
                    }
                }
            }
        }
    }

    private void deleteOldScore(Node worthlessNode) {

        Node parentNode = worthlessNode.getParent();

        if (worthlessNode.leftChild == null && worthlessNode.rightChild == null) {
            if (worthlessNode == root) {
                root = null;
            } else if (worthlessNode.isLeftChild()) {
                parentNode.leftChild = null;
            } else {
                parentNode.rightChild = null;
            }
        }

        else if (worthlessNode.rightChild == null) {
            if (worthlessNode == root) {
                root = worthlessNode.leftChild;
            } else if (worthlessNode.isLeftChild()) {
                parentNode.leftChild = worthlessNode.leftChild;
                worthlessNode.leftChild.parent = parentNode;
                worthlessNode.leftChild.isLeft = true;
            } else {
                parentNode.rightChild = worthlessNode.leftChild;
                worthlessNode.leftChild.parent = parentNode;
                worthlessNode.leftChild.isLeft = false;
            }
        }

        else if (worthlessNode.leftChild == null) {
            if (worthlessNode == root) {
                root = worthlessNode.rightChild;
            } else if (worthlessNode.isLeftChild()) {
                parentNode.leftChild = worthlessNode.rightChild;
                worthlessNode.rightChild.parent = parentNode;
                worthlessNode.rightChild.isLeft = true;
            } else {
                parentNode.rightChild = worthlessNode.rightChild;
                worthlessNode.rightChild.parent = parentNode;
                worthlessNode.rightChild.isLeft = false;
            }
        }

        else {
            Node replacement = getReplacementNode(worthlessNode);

            if (worthlessNode == root) {
                root = replacement;
            } else if (worthlessNode.isLeftChild()) {
                parentNode.leftChild = replacement;
                replacement.parent = parentNode;
                replacement.isLeft = true;
            } else {
                parentNode.rightChild = replacement;
                replacement.parent = parentNode;
                replacement.isLeft = false;
            }

            replacement.leftChild = worthlessNode.leftChild;
            worthlessNode.leftChild.parent = replacement;
            worthlessNode.isLeft = true;
        }
    }

    private Node getReplacementNode(Node replacingNode) {
        Node replaceParent = replacingNode;
        Node replacement = replacingNode;

        Node focusNode = replacingNode.rightChild;

        while(focusNode != null) {
            replaceParent = replacement;
            replacement = focusNode;
            focusNode = focusNode.leftChild;
        }

        if (replacement != replacingNode.rightChild) {
            replaceParent.leftChild = replacement.rightChild;
            replacement.rightChild = replacingNode.rightChild;
        }

        return replacement;
    }

    public List<Node> getTopEntrants(Node focusNode, int goTo) {
        if (topEntrants.size() <= goTo) {
            if (focusNode != null) {

                getTopEntrants(focusNode.rightChild, goTo);
                topEntrants.add(focusNode);
                getTopEntrants(focusNode.leftChild, goTo);

            }
        }

        return topEntrants;
    }

    public int size() {
        return playerRegister.size();
    }

    public void resetLeaderboard() {
        topEntrants.clear();
    }

    public Node get(int index) {
        topEntrants.clear();
        getTopEntrants(root, index);
        return topEntrants.get(index);
    }
}

class Node {

    float length;
    boolean isLeft;
    Fish fish;
    Player fisher;

    Node leftChild;
    Node rightChild;
    Node parent;

    Node(float fishLength, Fish fish, Player fisher) {
        this.length = fishLength;
        this.fish = fish;
        this.fisher = fisher;
    }

    public float getLength() {
        return length;
    }

    public Fish getFish() {
        return fish;
    }

    public Player getFisher() {
        return fisher;
    }

    public boolean isLeftChild() {
        return this.isLeft;
    }

    public Node getParent() {
        return this.parent;
    }
}
