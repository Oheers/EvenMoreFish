package com.oheers.fish.competition;

import com.oheers.fish.fishing.items.Fish;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LeaderboardTree {

    Node root;
    HashMap<Player, Float> playerRegister = new HashMap<>();
    List<Node> topEntrants = new ArrayList<>();

    private boolean shouldAdd(Player fisher, Float length) {
        if (!playerRegister.containsKey(fisher)) {
            System.out.println("uncontained");
            return true;
        } else {
            System.out.println("contained");
            if (playerRegister.get(fisher) < length) {
                System.out.println("<<<");
                deleteOldScore(fisher, length);
                return true;
            } else return false;
        }
    }

    private void addToRegister(Player fisher, Float length) {
        System.out.println("adding to register");
        playerRegister.put(fisher, length);
    }

    public void addNode(Fish fish, Player fisher) {
        System.out.println("+++++++++++++++++++++++ adding " + fish.getName());
        if (shouldAdd(fisher, fish.getLength())) {
            System.out.println("should add");
            addToRegister(fisher, fish.getLength());
            Node newNode = new Node(fish.getLength(), fish, fisher);

            if (root == null) root = newNode;
            else {
                System.out.println("root not null");
                Node focusNode = root;
                Node parentNode;

                while(true) {
                    parentNode = focusNode;
                    if (newNode.length < focusNode.length) {

                        focusNode = focusNode.leftChild;
                        if (focusNode == null) {
                            parentNode.leftChild = newNode;
                            return;
                        }
                    } else {
                        focusNode = focusNode.rightChild;

                        if (focusNode == null) {
                            parentNode.rightChild = newNode;
                            return;
                        }
                    }
                }
            }
        }
    }

    private void deleteOldScore(Player fisher, Float length) {
        System.out.println("deleting old score");

        Node focusNode = root;
        Node parentNode = root;

        boolean isLeftChild = true;

        while (focusNode.getFisher() != fisher) {

            parentNode = focusNode;

            if (length < focusNode.getLength()) {
                isLeftChild = true;
                focusNode = focusNode.leftChild;
            } else {
                isLeftChild = false;
                focusNode = focusNode.rightChild;
            }

            if (focusNode == null) {
                return;
            }

            if (focusNode.leftChild == null) return;
        }

        if (focusNode.leftChild == null && focusNode.rightChild == null) {
            if (focusNode == root) {
                root = null;
            } else if (isLeftChild) {
                parentNode.leftChild = null;
            } else {
                parentNode.rightChild = null;
            }
        }

        else if (focusNode.rightChild == null) {
            if (focusNode == root) {
                root = focusNode.leftChild;
            } else if (isLeftChild) {
                parentNode.leftChild = focusNode.leftChild;
            } else {
                parentNode.rightChild = focusNode.leftChild;
            }
        }

        else if (focusNode.leftChild == null) {
            if (focusNode == root) {
                root = focusNode.rightChild;
            } else if (isLeftChild) {
                parentNode.leftChild = focusNode.rightChild;
            } else {
                parentNode.rightChild = focusNode.rightChild;
            }
        }

        else {
            Node replacement = getReplacementNode(focusNode);

            if (focusNode == root) {
                root = replacement;
            } else if (isLeftChild) {
                parentNode.leftChild = replacement;
            } else {
                parentNode.rightChild = replacement;
            }

            replacement.leftChild = focusNode.leftChild;
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
                System.out.println("getTopEntrants: " + focusNode.getFish().getLength());
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
    Fish fish;
    Player fisher;

    Node leftChild;
    Node rightChild;

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
}
