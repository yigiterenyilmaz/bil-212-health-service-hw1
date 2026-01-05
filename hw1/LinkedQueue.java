public class LinkedQueue<E> implements Queue<E> {
    
    private static class Node<E> {
        private E element;
        private Node<E> next;
        
        public Node(E e, Node<E> n) {
            element = e;
            next = n;
        }
        
        public E getElement() {
            return element;
        }
        
        public Node<E> getNext() {
            return next;
        }
        
        public void setNext(Node<E> n) {
            next = n;
        }
    }
    
    private Node<E> head = null;
    private Node<E> tail = null;
    private int size = 0;
    
    public LinkedQueue() {
    }
    
    public int size() {
        return size;
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public E first() {
        if (isEmpty()) {
            return null;
        }
        return head.getElement();
    }
    
    public void enqueue(E element) {
        Node<E> newest = new Node<>(element, null);
        if (isEmpty()) {
            head = newest;
        } else {
            tail.setNext(newest);
        }
        tail = newest;
        size++;
    }
    
    public E dequeue() {
        if (isEmpty()) {
            return null;
        }
        E answer = head.getElement();
        head = head.getNext();
        size--;
        if (size == 0) {
            tail = null;
        }
        return answer;
    }
}
