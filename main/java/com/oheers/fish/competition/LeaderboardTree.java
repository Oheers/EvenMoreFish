package com.oheers.fish.competition;

import com.oheers.fish.fishing.items.Fish;
import org.bukkit.entity.Player;

public class LeaderboardTree {

    Node root;

    public void addNode(Fish fish, Player fisher) {

        Node newNode = new Node(fish.getLength(), fish, fisher);

        if (root == null) root = newNode;
        else {
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

    // @TODO
    public int size() {
        return 0;
    }

    // @TODO
    public Node get(int index) {
        return null;
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
