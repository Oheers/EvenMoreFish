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
    List<Float> currentValues = new ArrayList<>();
    CompetitionType competitionType;

    private boolean shouldAdd(Player fisher, Float length) {
        if (!playerRegister.containsKey(fisher)) {
            return true;
        } else {
            if (playerRegister.get(fisher).getValue() < length) {
                deleteOldScore(playerRegister.get(fisher));
                return true;
            } else return false;
        }
    }

    private void addToRegister(Player fisher, Node node) {
        playerRegister.put(fisher, node);
    }

    public void addNode(Fish fish, Player fisher) {
        if (competitionType == CompetitionType.LARGEST_FISH) {
            if (!shouldAdd(fisher, fish.getLength())) {
                return;
            }
        }

        Node newNode;
        if (competitionType == CompetitionType.LARGEST_FISH) {
            newNode = new Node(fish.getLength(), fish, fisher);
        } else {
            if (playerRegister.containsKey(fisher)) {
                deleteOldScore(playerRegister.get(fisher));
                newNode = playerRegister.get(fisher);
                newNode.value += 1;
            } else {
                newNode = new Node(1, null, fisher);
            }
        }

        if (checkDuplicate(newNode)) return;
        else currentValues.add(newNode.getValue());

        if (root == null) {
            root = newNode;
            addToRegister(fisher, root);
        }
        else {
            Node focusNode = root;
            Node parentNode;

            while(true) {
                parentNode = focusNode;
                if (newNode.value < focusNode.value) {

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

    private void deleteOldScore(Node worthlessNode) {

        if (worthlessNode.hasDuplicates()) {
            worthlessNode.useDuplicateValue();
            System.out.println(worthlessNode.duplicates.get(0) + " will be replacing " + worthlessNode);
            return;
        }

        Node parentNode = worthlessNode.getParent();

        if (worthlessNode.leftChild == null && worthlessNode.rightChild == null) {
            if (worthlessNode == root) {
                // all these checks are to allow duplicate values in this BST, predominantly in the MOST_FISH competition type
                currentValues.remove(worthlessNode.value); worthlessNode.handleDeletion(); root = null; // removing root.value since there's no duplicates of this value
            } else if (worthlessNode.isLeftChild()) {
                currentValues.remove(worthlessNode.value); parentNode.leftChild = null;
            } else {
                currentValues.remove(worthlessNode.value); parentNode.rightChild = null;
            }
        }

        else if (worthlessNode.rightChild == null) {
            if (worthlessNode == root) {
                currentValues.remove(worthlessNode.value); worthlessNode.handleDeletion(); root = worthlessNode.leftChild;
            } else if (worthlessNode.isLeftChild()) {
                currentValues.remove(worthlessNode.value);
                worthlessNode.handleDeletion();
                parentNode.leftChild = worthlessNode.leftChild;
                worthlessNode.leftChild.parent = parentNode;
                worthlessNode.leftChild.isLeft = true;
            } else {
                currentValues.remove(worthlessNode.value);
                worthlessNode.handleDeletion();
                parentNode.rightChild = worthlessNode.leftChild;
                worthlessNode.leftChild.parent = parentNode;
                worthlessNode.leftChild.isLeft = false;
            }
        }

        else if (worthlessNode.leftChild == null) {
            if (worthlessNode == root) {
                currentValues.remove(worthlessNode.value); worthlessNode.handleDeletion(); root = worthlessNode.rightChild;
            } else if (worthlessNode.isLeftChild()) {
                currentValues.remove(worthlessNode.value);
                worthlessNode.handleDeletion();
                parentNode.leftChild = worthlessNode.rightChild;
                worthlessNode.rightChild.parent = parentNode;
                worthlessNode.rightChild.isLeft = true;
            } else {
                currentValues.remove(worthlessNode.value);
                worthlessNode.handleDeletion();
                parentNode.rightChild = worthlessNode.rightChild;
                worthlessNode.rightChild.parent = parentNode;
                worthlessNode.rightChild.isLeft = false;
            }
        }

        else {
            Node replacement = getReplacementNode(worthlessNode);

            if (worthlessNode == root) {
                currentValues.remove(worthlessNode.value); worthlessNode.handleDeletion(); root = replacement;
            } else if (worthlessNode.isLeftChild()) {
                currentValues.remove(worthlessNode.value);
                worthlessNode.handleDeletion();
                parentNode.leftChild = replacement;
                replacement.parent = parentNode;
                replacement.isLeft = true;
            } else {
                currentValues.remove(worthlessNode.value);
                worthlessNode.handleDeletion();
                parentNode.rightChild = replacement;
                replacement.parent = parentNode;
                replacement.isLeft = false;
            }

            currentValues.remove(worthlessNode.value);
            worthlessNode.handleDeletion();
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
                System.out.println(focusNode + " is entering the leaderboard normally");
                if (focusNode.hasDuplicates()) {
                    for (Node n : focusNode.getDuplicates()) {
                        System.out.println(n + " is entering the leaderboard via duplicated");
                        if (topEntrants.size() <= goTo) topEntrants.add(n);
                    }
                }
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

    public boolean checkDuplicate(Node node) {
        // If the value exists *somewhere* in the BST, bother scanning through the player register
        if (currentValues.contains(node.getValue())) {
            System.out.println(node.value + " has tested positive for existing in the currentValues");
            // finding the node that has a duplicate value and adding that to the node's recognised duplicates
            for (Node n : playerRegister.values()) {
                if (n.scanDuplicate(node)) return true;
            }
        }
        return false;
    }
}

class Node {

    float value;
    boolean isLeft;
    Fish fish;
    Player fisher;

    Node leftChild;
    Node rightChild;
    Node parent;
    Node duplicateNode;

    List<Node> duplicates = new ArrayList<>();

    Node(float value, Fish fish, Player fisher) {
        this.value = value;
        this.fish = fish;
        this.fisher = fisher;
    }

    public float getValue() {
        return value;
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

    public List<Node> getDuplicates() {
        return this.duplicates;
    }

    public boolean scanDuplicate(Node node) {
        if (node.getValue() == this.value) {
            duplicates.add(node);
            node.duplicateNode = this;
            System.out.println("adding: " + node + " to the duplicates section of: " + this);
            return true;
        } else return false;
    }

    public boolean hasDuplicates() {
        if (duplicates.size() != 0) {
            System.out.println(this + " has " + duplicates.size() + " duplicates");
            return true;
        }
        else {
            System.out.println(this + " has no duplicates");
            return duplicates.size() != 0;
        }
    }

    // shifts a value from the duplicate list to represent this node
    public void useDuplicateValue() {
        Node selected = duplicates.get(0);
        fish = selected.getFish();
        fisher = selected.getFisher();
        duplicates.remove(selected);
        System.out.println(selected + " is taking over from " + this);
    }

    @Override
    public String toString() {
        String s = "Node[Fisher: " + fisher.getName();
        if (fish != null) s += ", Fish: " + fish.getRarity().getValue() + " " + fish.getName();
        s += ", " + "Value: " + value + "]";
        return s;
    }

    // When deleting a node, in some cases the node won't be set to null
    public void handleDeletion() {
        if (duplicateNode != null) {
            System.out.println("removing " + this + " from the duplication list of " + duplicateNode);
            duplicateNode.duplicates.remove(this);
        }
    }
}
