package com.loopers.domain.Like.event;


public enum LikeEventType {
    CREATED {
        @Override
        public int delta() {
            return 1;
        }
    },
    DELETED {
        @Override
        public int delta() {
            return -1;
        }
    };

    public abstract int delta();
}
