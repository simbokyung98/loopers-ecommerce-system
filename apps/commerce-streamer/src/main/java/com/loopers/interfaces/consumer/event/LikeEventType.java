package com.loopers.interfaces.consumer.event;


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
