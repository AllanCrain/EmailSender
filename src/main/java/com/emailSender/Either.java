package com.emailSender;

public class Either<L, R> {

    private L left;
    private R right;

    public Either(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public boolean hasRight() {
        return this.right != null;
    }

    public boolean hasLeft() {
        return this.left != null;
    }

    public R getRight() {
        return this.right;
    }

    public L getLeft() {
        return this.left;
    }
}
