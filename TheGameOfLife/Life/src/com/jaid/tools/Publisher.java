package com.jaid.tools;

public class Publisher {
    public interface Distributor {
        void deliverTo(Object subscriber); // the Visitor pattern's "visit" method.
    }


    private class Node {
        public final Object subscriber;
        public final Node next;

        private Node(Object subscriber, Node next) {
            this.subscriber = subscriber;
            this.next = next;
        }

        public Node remove(Object target) {
            if (target == subscriber)
                return next;

            if (next == null)                        // target is not in list
                throw new java.util.NoSuchElementException
                        (target.toString());

            return new Node(subscriber, next.remove(target));
        }

        public void accept(Distributor deliveryAgent) // deliveryAgent is a "visitor"
        {
            deliveryAgent.deliverTo(subscriber);
        }
    }

    private volatile Node subscribers = null;


    public void publish(Distributor deliveryAgent) {
        for (Node cursor = subscribers; cursor != null; cursor = cursor.next)
            cursor.accept(deliveryAgent);
    }

    synchronized public void subscribe(Object subscriber) {
        subscribers = new Node(subscriber, subscribers);
    }

    synchronized public void cancelSubscription(Object subscriber) {
        subscribers = subscribers.remove(subscriber);
    }
}