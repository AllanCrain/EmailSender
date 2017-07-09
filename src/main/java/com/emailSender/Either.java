package com.emailSender;

import java.util.NoSuchElementException;

/**
 * A minimal implementation of Either monad. Inspired by https://bitbucket.org/atlassian/fugue/src/b0868a00273f8f2ccf5d1a2610a8a5507ce641c4/fugue/src/main/java/com/atlassian/fugue/Either.java?at=master
 */
public abstract class Either<L, R> {

    public static <L, R> Either<L, R> left(L left) {
        if (left == null) throw new IllegalArgumentException("cannot be null");
        return new Left<>(left);
    }

    public static <L, R> Either<L, R> right(R right) {
        if (right == null) throw new IllegalArgumentException("cannot be null");
        return new Right<>(right);
    }

    public abstract boolean hasLeft();

    public abstract boolean hasRight();

    public abstract L getLeft();

    public abstract R getRight();

    public static final class Left<L, R> extends Either<L, R> {

        private final L left;

        public Left(L left) {
            this.left = left;
        }

        @Override
        public boolean hasLeft() {
            return true;
        }

        @Override
        public boolean hasRight() {
            return false;
        }

        @Override
        public L getLeft() {
            return this.left;
        }

        @Override
        public R getRight() {
            throw new NoSuchElementException();
        }
    }

    public static final class Right<L, R> extends Either<L, R> {

        private final R right;

        public Right(R right) {
            this.right = right;
        }

        @Override
        public boolean hasLeft() {
            return false;
        }

        @Override
        public boolean hasRight() {
            return true;
        }

        @Override
        public R getRight() {
            return this.right;
        }

        @Override
        public L getLeft() {
            throw new NoSuchElementException();
        }
    }
}