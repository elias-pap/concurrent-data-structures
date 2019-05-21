/*
The Persistent Non-Blocking Binary Search Tree

This is an implementation of the PNB-BST algorithm described in the paper
"Persistent Non-Blocking Binary Search Trees Supporting Wait-Free Range Queries"
of Panagiota Fatourou, Eric Ruppert and Elias Papavasileiou

Copyright (C) 2019  Elias Papavasileiou

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;  // For CAS operations
import java.util.concurrent.atomic.AtomicInteger;                // For atomic increments of counter

/**
    Represents a Persistent Non-Blocking Binary Search Tree (PNB-BST)
*/
public class LockFreePBSTMap<K extends Comparable<? super K>, V> {

    /**
        Represents a PNB-BST node.
    */
    private static final class Node<X extends Comparable<? super X>, Y> {
        private X key;                  // The unique key of this node
        private Y value;                // Data that map to the key
        volatile Node<X,Y> leftChild;   // Reference to the left child of this node
        volatile Node<X,Y> rightChild;  // Reference to the right child of this node
        private Node<X,Y> prevNode;     // Reference to the node replaced by this node
        volatile Info<X,Y> info;        // Reference to the Info object that this node belongs
        private int versionSeq;         // The sequence number of this node

        /**
            Private constructor. Used by others to create
            either a Leaf or an Internal PNB-BST node.
        */
        private Node(final X key, final Y value, final Node<X,Y> leftChild, final Node<X,Y> rightChild,
                     final Node<X,Y> prevNode, final Info<X,Y> info, final int versionSeq) {
            this.key = key;
            this.value = value;
            this.leftChild = leftChild;
            this.rightChild = rightChild;
            this.prevNode = prevNode;
            this.info = info;
            this.versionSeq = versionSeq;
        }

        /**
            Creates a Leaf node.
        */
        Node(final X key, final Y value, final Node<X,Y> prevNode, final Info<X,Y> info, final int versionSeq) {
            this(key, value, null, null, prevNode, info, versionSeq);
        }

        /**
            Creates an Internal node.
        */
        Node(final X key, final Node<X,Y> leftChild, final Node<X,Y> rightChild,
             final Node<X,Y> prevNode, final Info<X,Y> info, final int versionSeq) {
            this(key, null, leftChild, rightChild, prevNode, info, versionSeq);
        }

        /**
            Checks if this node is frozen for an Info object.

            @param info   the info object for which to check this node's frozen status
            @return       true if this node if frozen for the provided info, false otherwise
        */
        private boolean frozen(final Info<X,Y> info) {
            return ((info.state == State.NULL || info.state == State.TRY)    // Operation is in progress - needs help
                   || (info.isMarked(this) && info.state == State.COMMIT));  // This node is deleted from the tree
        }
    }

    /**
        Represents the possible states that an Info object can be in.

        NULL:   The procedure of updating a child pointer of the first node
                is in progress, but the handshaking has not been performed yet.
        TRY:    The procedure of updating a child pointer of the first node
                is in progress, and the handshaking has completed successfully.
        COMMIT: The marked nodes have been deleted and the first node is idle.
        ABORT:  All nodes are idle.
    */
    private enum State {
        NULL, TRY, COMMIT, ABORT
    }

    /**
        Represents an Info object.
    */
    private static final class Info<X extends Comparable<? super X>, Y> {
        // The state of this Info object
        volatile State state;

        // Node whose child pointer will be updated
        // Connects the newNode to the tree
        private Node<X,Y> connectorNode;

        // Nodes to be deleted (marked nodes)
        private Node<X,Y> firstMarkedNode;    // Used in Insert and Delete
        private Node<X,Y> secondMarkedNode;   // Used in Delete only
        private Node<X,Y> thirdMarkedNode;    // Used in Delete only

        // Saved info references of marked nodes
        // Used as expected values in info CAS
        private Info<X,Y> firstMarkedOldInfo;   // Used in Insert and Delete
        private Info<X,Y> secondMarkedOldInfo;  // Used in Delete only
        private Info<X,Y> thirdMarkedOldInfo;   // Used in Delete only

        // Node to be connected to the tree
        // Can be the Internal of the new triad (on Insert)
        // or the new sibling (on Delete)
        // Used as update value in child CAS
        private Node<X,Y> newNode;

        // Sequence number of the Insert or Delete operation
        // Used for handshaking
        private int handshakingSeq;

        /**
            Creates an Info object for a Delete operation.
        */
        Info(final State state, final Node<X,Y> connectorNode,
             final Node<X,Y> firstMarkedNode, final Node<X,Y> secondMarkedNode, final Node<X,Y> thirdMarkedNode,
             final Info<X,Y> firstMarkedOldInfo, final Info<X,Y> secondMarkedOldInfo, final Info<X,Y> thirdMarkedOldInfo,
             final Node<X,Y> newNode, final int handshakingSeq) {
            this.state = state;
            this.connectorNode = connectorNode;
            this.firstMarkedNode = firstMarkedNode;
            this.secondMarkedNode = secondMarkedNode;
            this.thirdMarkedNode = thirdMarkedNode;
            this.firstMarkedOldInfo = firstMarkedOldInfo;
            this.secondMarkedOldInfo = secondMarkedOldInfo;
            this.thirdMarkedOldInfo = thirdMarkedOldInfo;
            this.newNode = newNode;
            this.handshakingSeq = handshakingSeq;
        }

        /**
            Creates an Info object for an Insert operation.
        */
        Info(final State state, final Node<X,Y> connectorNode, final Node<X,Y> firstMarkedNode,
             final Info<X,Y> firstMarkedOldInfo, final Node<X,Y> newNode, final int handshakingSeq) {
            this(state, connectorNode, firstMarkedNode, null, null, firstMarkedOldInfo, null, null, newNode, handshakingSeq);
        }

        /**
            Checks if the node is marked for this Info object.

            @param node    the node to check its mark status
        */
        private boolean isMarked(final Node<X,Y> node) {
            return (this.firstMarkedNode == node || this.secondMarkedNode == node || this.thirdMarkedNode == node);
        }
    }

    // Updater objects, used for CAS operations
    private static final AtomicReferenceFieldUpdater<Node, Node> leftChildUpdater =
                         AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "leftChild");
    private static final AtomicReferenceFieldUpdater<Node, Node> rightChildUpdater =
                         AtomicReferenceFieldUpdater.newUpdater(Node.class, Node.class, "rightChild");
    private static final AtomicReferenceFieldUpdater<Node, Info> infoUpdater =
                         AtomicReferenceFieldUpdater.newUpdater(Node.class, Info.class, "info");
    private static final AtomicReferenceFieldUpdater<Info, State> stateUpdater =
                         AtomicReferenceFieldUpdater.newUpdater(Info.class, State.class, "state");

    // Global version counter
    private AtomicInteger counter = new AtomicInteger(0);

    // The Dummy Info object
    private static final Info dummy = new Info(State.ABORT, null, null, null, null, null, null, null, null, 0);

    // To avoid handling special case when the tree contains <= 2 nodes,
    // 2 dummy nodes (children of root) are created that both contain key null.
    // All real keys inside PBST are required to be non-null
    private final Node<K,V> root = new Node<K,V>(null, new Node<K,V>(null, null, null, dummy, 0),
                                   new Node<K,V>(null, null, null, dummy, 0), null, dummy, 0);

    // Reference to thread local variables that are used by
    // ValidateLeaf and ValidateLink to return the result of a validation
    private final ThreadLocal<ValidationResultHolder> validationResult = new ThreadLocal<ValidationResultHolder>() {
        @Override
        protected ValidationResultHolder initialValue() {
            return new ValidationResultHolder<K,V>();
        }
    };

    // Reference to a thread local variable that is used by
    // RangeScan to return the result of a range query
    private final ThreadLocal<RangeScanResultHolder> rangeScanResult = new ThreadLocal<RangeScanResultHolder>() {
        @Override
        protected RangeScanResultHolder initialValue() {
            return new RangeScanResultHolder<K,V>();
        }
    };

    // Reference to thread local variables that are used by
    // putIfAbsent to create one new triad per successful call
    private final ThreadLocal<InsertTriadHolder> insertTriad = new ThreadLocal<InsertTriadHolder>() {
        @Override
        protected InsertTriadHolder initialValue() {
            return new InsertTriadHolder<K,V>();
        }
    };

    // Reference to thread local variables that are used by
    // remove to create one new copy of sibling per successful call
    private final ThreadLocal<SiblingCopyHolder> siblingCopy = new ThreadLocal<SiblingCopyHolder>() {
        @Override
        protected SiblingCopyHolder initialValue() {
            return new SiblingCopyHolder<K,V>();
        }
    };

    // Reference to thread local variable that is used by
    // executeInsert and executeDelete to reuse info objects
    private final ThreadLocal<InfoObjectHolder> infoObject = new ThreadLocal<InfoObjectHolder>() {
        @Override
        protected InfoObjectHolder initialValue() {
            return new InfoObjectHolder<K,V>();
        }
    };

    /**
        Represents a storage space where the result of a validation operation is saved
        Each thread gets a copy of these variables
    */
    private static final class ValidationResultHolder<X extends Comparable<? super X>, Y> {
        private ValidateLeafResult<X,Y> gp_p_l_link;
        private ValidateLinkResult<X,Y> gp_p_link;
        private ValidateLinkResult<X,Y> p_l_link;
        private ValidateLinkResult<X,Y> p_s_link;
        private ValidateLinkResult<X,Y> s_newLeft_link;
        private ValidateLinkResult<X,Y> s_newRight_link;

        ValidationResultHolder() {
            gp_p_l_link = new ValidateLeafResult<X,Y>(false, null, null);
            gp_p_link = new ValidateLinkResult<X,Y>(false, null);
            p_l_link = new ValidateLinkResult<X,Y>(false, null);
            p_s_link = new ValidateLinkResult<X,Y>(false, null);
            s_newLeft_link = new ValidateLinkResult<X,Y>(false, null);
            s_newRight_link = new ValidateLinkResult<X,Y>(false, null);
        }

        private static final class ValidateLinkResult<X extends Comparable<? super X>, Y> {
            boolean validated;
            Info<X,Y> info;

            ValidateLinkResult(boolean validated, Info<X,Y> info) {
                this.validated = validated;
                this.info = info;
            }
        }

        private static final class ValidateLeafResult<X extends Comparable<? super X>, Y> {
            boolean validated;
            Info<X,Y> gpinfo;
            Info<X,Y> pinfo;

            ValidateLeafResult(boolean validated, Info<X,Y> gpinfo, Info<X,Y> pinfo) {
                this.validated = validated;
                this.gpinfo = gpinfo;
                this.pinfo = pinfo;
            }
        }
    }

    /**
        Represents a storage space where the result of a range query operation is saved
        Each thread gets a copy of this variable
    */
    private static final class RangeScanResultHolder<X extends Comparable<? super X>, Y> {
        private Stack<Y> rsResult;

        RangeScanResultHolder() {
            rsResult = new Stack<Y>();
        }

        private static final class Stack<Y> {
            private final int INIT_SIZE = 128;
            private Object[] stackArray;
            private int head = 0;

            Stack() {
                stackArray = new Object[INIT_SIZE];
            }

            final void clear() {
                head = 0;
            }

            final Object[] getStackArray() {
                return stackArray;
            }

            final int getEffectiveSize() {
                return head;
            }

            final void push(final Y x) {
                if (head == stackArray.length) {
                    final Object[] newStackArray = new Object[stackArray.length*4];
                    System.arraycopy(stackArray, 0, newStackArray, 0, head);
                    stackArray = newStackArray;
                }
                stackArray[head] = x;
                ++head;
            }
        }
    }

    /**
        Represents a storage space where the new nodes created by putIfAbsent are saved
        Each thread gets a copy of these variables
    */
    private static final class InsertTriadHolder<X extends Comparable<? super X>, Y> {
        private Node<X,Y> newInternal;
        private Node<X,Y> newLeaf;
        private Node<X,Y> newSibling;

        InsertTriadHolder() {
            newInternal = null;
            newLeaf = null;
            newSibling = null;
        }

        void refreshNewLeaf(final X key, final Y value, final int versionSeq) {
            refreshLeaf(newLeaf, key, value, versionSeq);
        }

        void refreshNewSibling(final X key, final Y value, final int versionSeq) {
            refreshLeaf(newSibling, key, value, versionSeq);
        }

        void refreshNewInternal(final X key, final Node<X,Y> leftChild, final Node<X,Y> rightChild,
                                final Node<X,Y> prevNode, final int versionSeq) {
            newInternal.key = key;
            newInternal.leftChild = leftChild;
            newInternal.rightChild = rightChild;
            newInternal.prevNode = prevNode;
            newInternal.info = dummy;
            newInternal.versionSeq = versionSeq;
        }

        private void refreshLeaf(final Node<X,Y> leaf, final X key, final Y value, final int versionSeq) {
            leaf.key = key;
            leaf.value = value;
            leaf.prevNode = null;
            leaf.info = dummy;
            leaf.versionSeq = versionSeq;
        }
    }

    /**
        Represents a storage space where the new node created by remove is saved
        Each thread gets a copy of this variable
    */
    private static final class SiblingCopyHolder<X extends Comparable<? super X>, Y> {
        private Node<X,Y> newSibling;

        SiblingCopyHolder() {
            newSibling = null;
        }

        void refreshInternal(final X key, final Node<X,Y> leftChild, final Node<X,Y> rightChild,
                                final Node<X,Y> prevNode, final int versionSeq) {
            newSibling.key = key;
            newSibling.value = null;
            newSibling.leftChild = leftChild;
            newSibling.rightChild = rightChild;
            newSibling.prevNode = prevNode;
            newSibling.info = dummy;
            newSibling.versionSeq = versionSeq;
        }

        void refreshLeaf(final X key, final Y value, final Node<X,Y> prevNode, final int versionSeq) {
            newSibling.key = key;
            newSibling.value = value;
            newSibling.leftChild = null;
            newSibling.rightChild = null;
            newSibling.prevNode = prevNode;
            newSibling.info = dummy;
            newSibling.versionSeq = versionSeq;
        }
    }

    /**
        Represents a storage space where the new info object created by executeInsert or executeDelete is saved
        Each thread gets a copy of this variable
    */
    private static final class InfoObjectHolder<X extends Comparable<? super X>, Y> {
        private Info<X,Y> info;
        private boolean shallCreateNewInfo;

        InfoObjectHolder() {
            info = null;
            shallCreateNewInfo = true;
        }

        void refreshInsertInfo(final State state, final Node<X,Y> connectorNode, final Node<X,Y> firstMarkedNode,
                               final Info<X,Y> firstMarkedOldInfo, final Node<X,Y> newNode, final int handshakingSeq) {
            refreshInfo(state, connectorNode, firstMarkedNode, null, null, firstMarkedOldInfo,
                        null, null, newNode, handshakingSeq);
        }

        void refreshDeleteInfo(final State state, final Node<X,Y> connectorNode, final Node<X,Y> firstMarkedNode,
                               final Node<X,Y> secondMarkedNode, final Node<X,Y> thirdMarkedNode,
                               final Info<X,Y> firstMarkedOldInfo, final Info<X,Y> secondMarkedOldInfo,
                               final Info<X,Y> thirdMarkedOldInfo, final Node<X,Y> newNode, final int handshakingSeq) {
            refreshInfo(state, connectorNode, firstMarkedNode, secondMarkedNode, thirdMarkedNode,
                        firstMarkedOldInfo, secondMarkedOldInfo, thirdMarkedOldInfo, newNode, handshakingSeq);
        }

        private void refreshInfo(final State state, final Node<X,Y> connectorNode,
             final Node<X,Y> firstMarkedNode, final Node<X,Y> secondMarkedNode, final Node<X,Y> thirdMarkedNode,
             final Info<X,Y> firstMarkedOldInfo, final Info<X,Y> secondMarkedOldInfo, final Info<X,Y> thirdMarkedOldInfo,
             final Node<X,Y> newNode, final int handshakingSeq) {
            info.state = state;
            info.connectorNode = connectorNode;
            info.firstMarkedNode = firstMarkedNode;
            info.secondMarkedNode = secondMarkedNode;
            info.thirdMarkedNode = thirdMarkedNode;
            info.firstMarkedOldInfo = firstMarkedOldInfo;
            info.secondMarkedOldInfo = secondMarkedOldInfo;
            info.thirdMarkedOldInfo = thirdMarkedOldInfo;
            info.newNode = newNode;
            info.handshakingSeq = handshakingSeq;
        }
    }

    /**
        Implements the Find operation.
        <p>
        Precondition: key cannot be null.

        @param key  the key whose presence in this map is to be tested
        @return     true if this map contains a mapping for the specified key
    */
    public final boolean containsKey(final K key) {
        // Get validationResultHolder before the start of making attempts
        ValidationResultHolder validationResultHolder = validationResult.get();

        // Search variables
        int seq;
        Node<K,V> ggp = null, gp, p, l;

        // Start making search attempts
        while (true) {
            seq = counter.get();   // Update sequence number

            // Optimization - if ggp is not frozen, resume search from there
            if (ggp != null && !ggp.frozen(ggp.info)) {
                p = ggp;
                l = readChild(p, p.key == null || key.compareTo(p.key) < 0, seq);
            }
            else {    // Restart search from root
                p = root;
                l = root.leftChild;
            }
            ggp = null;
            gp = null;

            // Search for the leaf that may contain key
            while (l.leftChild != null) {
                ggp = gp;
                gp = p;
                p = l;
                l = readChild(p, p.key == null || key.compareTo(p.key) < 0, seq);
            }
            // After resuming from ggp, in case the nodes pointed by p and l are deleted
            // before the search starts, gp will be null but p will not point to the root node
            // (instead, it will point to the old ggp which, in the general case, is not the root).
            // This state is unacceptable, thus search must be restarted from root.
            if (gp == null && p != root) continue;

            // Perform validation
            validateLeaf(gp, p, l, key, validationResultHolder);

            // Proceed only if validation was successful
            if (validationResultHolder.gp_p_l_link.validated)
                return (l.key != null && key.compareTo(l.key) == 0);
        }
    }

    /**
        Implements the Insert operation.
        <p>
        Precondition: key cannot be null.

        @param key    the key with which the specified value is to be associated
        @param value  the value to be associated with the specified key
        @return       the previous value associated with the specified key,
                      or null if there was no mapping for the key.
    */
    public final V putIfAbsent(final K key, final V value) {
        // Get thread local variables before the start of making attempts
        ValidationResultHolder validationResultHolder = validationResult.get();
        InsertTriadHolder insertTriadHolder = insertTriad.get();

        // Search variables
        int seq;
        Node<K,V> ggp = null, gp, p, l;

        // Start making insert attempts
        while (true) {
            seq = counter.get();   // Update sequence number

            // Optimization - if ggp is not frozen, resume search from there
            if (ggp != null && !ggp.frozen(ggp.info)) {
                p = ggp;
                l = readChild(p, p.key == null || key.compareTo(p.key) < 0, seq);
            }
            else {    // Restart search from root
                p = root;
                l = root.leftChild;
            }
            ggp = null;
            gp = null;

            // Search for the leaf that may contain key
            while (l.leftChild != null) {
                ggp = gp;
                gp = p;
                p = l;
                l = readChild(p, p.key == null || key.compareTo(p.key) < 0, seq);
            }
            // After resuming from ggp, in case the nodes pointed by p and l are deleted
            // before the search starts, gp will be null but p will not point to the root node
            // (instead, it will point to the old ggp which, in the general case, is not the root).
            // This state is unacceptable, thus search must be restarted from root.
            if (gp == null && p != root) continue;

            // Perform validation
            validateLeaf(gp, p, l, key, validationResultHolder);

            // Proceed only if validation was successful
            if (validationResultHolder.gp_p_l_link.validated) {
                if (l.key != null && key.compareTo(l.key) == 0)
                    return l.value;    // Unsuccessful Insert

                // Optimization - extra handshaking check
                if (counter.get() != seq) continue;

                if (insertTriadHolder.newLeaf == null) {    // Create a new triad
                    insertTriadHolder.newLeaf = new Node<K,V>(key, value, null, dummy, seq);
                    insertTriadHolder.newSibling = new Node<K,V>(l.key, l.value, null, dummy, seq);
                    if (l.key == null || key.compareTo(l.key) < 0)
                        insertTriadHolder.newInternal = new Node<K,V>(l.key, insertTriadHolder.newLeaf,
                                                                      insertTriadHolder.newSibling, l, dummy, seq);
                    else
                        insertTriadHolder.newInternal = new Node<K,V>(key, insertTriadHolder.newSibling,
                                                                      insertTriadHolder.newLeaf, l, dummy, seq);
                }
                else {  // Refresh the already created triad
                    insertTriadHolder.refreshNewLeaf(key, value, seq);
                    insertTriadHolder.refreshNewSibling(l.key, l.value, seq);
                    if (l.key == null || key.compareTo(l.key) < 0)
                        insertTriadHolder.refreshNewInternal(l.key, insertTriadHolder.newLeaf,
                                                             insertTriadHolder.newSibling, l, seq);
                    else
                        insertTriadHolder.refreshNewInternal(key, insertTriadHolder.newSibling,
                                                             insertTriadHolder.newLeaf, l, seq);
                }

                // Attempt insertion
                if (executeInsert(p, l, validationResultHolder.gp_p_l_link.pinfo,
                                  l.info, insertTriadHolder.newInternal, seq)) {
                    insertTriad.remove();
                    return null;    // Successful Insert
                }
            }
        }
    }

    /**
        Implements the Delete operation.
        <p>
        Precondition: key cannot be null.

        @param key    the key whose mapping is to be removed from the map
        @return       the previous value associated with the specified key,
                      or null if there was no mapping for the key.
    */
    public final V remove(final K key) {
        // Get thread local variables before the start of making attempts
        ValidationResultHolder validationResultHolder = validationResult.get();
        SiblingCopyHolder siblingCopyHolder = siblingCopy.get();

        // Search variables
        int seq;
        Node<K,V> ggp = null, gp, p, l;

        // Helpers
        Node<K,V> sibling;
        Info<K,V> sinfo;
        boolean validated = false;

        // Start making delete attempts
        while (true) {
            seq = counter.get();   // Update sequence number

            // Optimization - if ggp is not frozen, resume search from there
            if (ggp != null && !ggp.frozen(ggp.info)) {
                p = ggp;
                l = readChild(p, p.key == null || key.compareTo(p.key) < 0, seq);
            }
            else {    // Restart search from root
                p = root;
                l = root.leftChild;
            }
            ggp = null;
            gp = null;

            // Search for the leaf that may contain key
            while (l.leftChild != null) {
                ggp = gp;
                gp = p;
                p = l;
                l = readChild(p, p.key == null || key.compareTo(p.key) < 0, seq);
            }
            // After resuming from ggp, in case the nodes pointed by p and l are deleted
            // before the search starts, gp will be null but p will not point to the root node
            // (instead, it will point to the old ggp which, in the general case, is not the root).
            // This state is unacceptable, thus search must be restarted from root.
            if (gp == null && p != root) continue;

            // Perform validation
            validateLeaf(gp, p, l, key, validationResultHolder);

            // Proceed only if validation was successful
            if (validationResultHolder.gp_p_l_link.validated) {
                if (l.key == null || key.compareTo(l.key) != 0)
                    return null;    // Unsuccessful Delete

                // Validate sibling
                sibling = readChild(p, p.key != null && (l.key).compareTo(p.key) >= 0, seq);
                validateLink(p, sibling, p.key != null && (l.key).compareTo(p.key) >= 0, validationResultHolder.p_s_link);
                validated = validationResultHolder.p_s_link.validated;
                if (validated) {
                    // Optimization - extra handshaking check
                    if (counter.get() != seq) continue;

                    if (sibling.leftChild == null) {    // sibling is a leaf node
                        if (siblingCopyHolder.newSibling == null)   // Create a new leaf sibling
                            siblingCopyHolder.newSibling = new Node<K,V>(sibling.key, sibling.value, p, dummy, seq);
                        else    // Refresh the already created leaf sibling
                            siblingCopyHolder.refreshLeaf(sibling.key, sibling.value, p, seq);
                    }
                    else if (siblingCopyHolder.newSibling == null)  // Create a new internal sibling
                        siblingCopyHolder.newSibling = new Node<K,V>(sibling.key, sibling.leftChild,
                                                                     sibling.rightChild, p, dummy, seq);
                    else    // Refresh the already created internal sibling
                        siblingCopyHolder.refreshInternal(sibling.key, sibling.leftChild, sibling.rightChild, p, seq);

                    if (sibling.leftChild != null) {    // Sibling is Internal, validate its children
                        validateLink(sibling, siblingCopyHolder.newSibling.leftChild,
                                     true, validationResultHolder.s_newLeft_link);
                        validated = validationResultHolder.s_newLeft_link.validated;
                        sinfo = validationResultHolder.s_newLeft_link.info;
                        if (validated) {
                            validateLink(sibling, siblingCopyHolder.newSibling.rightChild,
                                         false, validationResultHolder.s_newRight_link);
                            validated = validationResultHolder.s_newRight_link.validated;
                        }
                    }
                    else    // Sibling is Leaf
                        sinfo = sibling.info;

                    // Attempt deletion
                    if (validated && executeDelete(gp, p, l, sibling, validationResultHolder.gp_p_l_link.gpinfo,
                                                   validationResultHolder.gp_p_l_link.pinfo, l.info, sinfo,
                                                   siblingCopyHolder.newSibling, seq)) {
                        siblingCopy.remove();
                        return l.value;    // Successful Delete
                    }
                }
            }
        }
    }

    /**
        Implements the RangeScan operation.
        <p>
        Preconditions:
        <ul>
            <li> a and b cannot be null
            <li> a is less than or equal to b
        <ul>

        @param a  the lower limit of the range
        @param b  the upper limit of the range
        @return   all values of mappings with keys in range [a,b]
    */
    public final Object[] rangeScan(final K a, final K b) {
        int seq = counter.get();    // Get a sequence number
        counter.incrementAndGet();  // Increment global version counter

        // Get and initialize rangeScanResultHolder before the start of the tree traversal
        RangeScanResultHolder rangeScanResultHolder = rangeScanResult.get();
        rangeScanResultHolder.rsResult.clear();

        // Start the tree traversal
        scanHelper(root, seq, a, b, rangeScanResultHolder.rsResult);

        // Get stack and its number of elements
        Object[] stackArray = rangeScanResultHolder.rsResult.getStackArray();
        int stackSize = rangeScanResultHolder.rsResult.getEffectiveSize();

        // Make a copy of the stack and return it
        Object[] returnArray = new Object[stackSize];
        for (int i = 0; i < stackSize; i++)
            returnArray[i] = stackArray[i];
        return returnArray;
    }

    /**
        Helps a search traverse the version-seq part of the tree.
        Preconditions: p is not null and p.versionSeq is not greater than seq

        @param p      the parent of the node to be returned
        @param left   determines whether to search the list that begins with the left or right child of p
        @param seq    the maximum sequence number of the node to be returned
        @return       the left or right version-seq child of p
    */
    private final Node<K,V> readChild(final Node<K,V> p, final boolean left, final int seq) {
        // Find the left or right version-seq child of p
        Node<K,V> l = left ? p.leftChild : p.rightChild;
        while (l.versionSeq > seq) l = l.prevNode;

        // Return it
        return l;
    }

    /**
        Validates the link between p and c.
        Preconditions: p and c are not null

        @param p         the parent of the link to be validated
        @param c         the child of the link to be validated
        @param left      determines whether c should be the left or right child of p
        @param p_c_link  contains the validation result. When the validation succeeds,
                         validated is true and info equals to p's info initially read.
                         Otherwise, validated is false and info is null.
    */
    private final void validateLink(final Node<K,V> p, final Node<K,V> c, final boolean left,
                              final ValidationResultHolder.ValidateLinkResult<K,V> p_c_link) {
        // Initialize results
        p_c_link.validated = false;
        p_c_link.info = null;

        // Validate the link between p and c
        final Info<K,V> pinfo = p.info;
        if (pinfo.state == State.NULL || pinfo.state == State.TRY)   // p participates in an in-progress operation
            help(pinfo);
        else if (!(pinfo.isMarked(p) && pinfo.state == State.COMMIT)   // p is not deleted
            && ((left && c == p.leftChild) || (!left && c == p.rightChild))) {   // p is the father of c
            // Update results
            p_c_link.validated = true;
            p_c_link.info = pinfo;
        }
    }

    /**
        Validates the leaf l, by validating the links between gp-p and p-l.
        Preconditions: p and l are not null and if p is not root then gp is not null

        @param gp      the grandparent of the leaf to be validated
        @param p       the parent of the leaf to be validated
        @param l       the leaf to be validated
        @param k       the key of the caller operation
        @param vrh     contains the validation result. When the validation succeeds,
                       validated is true and gpinfo and pinfo equal to gp's and p's info, respectively.
                       Otherwise, validated is false and the values of gpinfo and pinfo are not needed.
    */
    private final void validateLeaf(final Node<K,V> gp, final Node<K,V> p, final Node<K,V> l, final K k,
                              final ValidationResultHolder<K,V> vrh) {
        // Initialization
        boolean validated = false;

        // Validation
        validateLink(p, l, p.key == null || k.compareTo(p.key) < 0, vrh.p_l_link);
        validated = vrh.p_l_link.validated;
        if (validated && p != root) {
            validateLink(gp, p, gp.key == null || k.compareTo(gp.key) < 0, vrh.gp_p_link);
            validated = vrh.gp_p_link.validated;
        }
        validated = validated && p.info == vrh.p_l_link.info && (p == root || gp.info == vrh.gp_p_link.info);

        // Results
        vrh.gp_p_l_link.validated = validated;
        vrh.gp_p_l_link.gpinfo = vrh.gp_p_link.info;
        vrh.gp_p_l_link.pinfo = vrh.p_l_link.info;
    }

    /**
        Executes the tree traversal for rangeScan.
        Precondition: node.versionSeq is not greater than seq

        @param node    the current node of the traversal
        @param seq     the sequence number of rangeScan operation
        @param a       the lower limit of the range
        @param b       the upper limit of the range
        @param ret     contains the rangeScan result, i.e. all values that correspond to keys
                       held by nodes in the version-seq part of the tree
    */
    private final void scanHelper(final Node<K,V> node, final int seq, final K a, final K b, RangeScanResultHolder.Stack<V> ret) {
        if (node.leftChild == null) {    // node is a leaf
            // In case the node's key falls between a and b, add it to the result of the range query
            if (node.key != null && a.compareTo(node.key) <= 0 && b.compareTo(node.key) >= 0)
                ret.push(node.value);
        }
        else {
            final Info<K,V> info = node.info;
            if (info.state == State.NULL || info.state == State.TRY)    // node participates in an in-progress operation
                help(info);
            if (node.key != null && a.compareTo(node.key) >= 0)           // node's key is below the lower limit of [a,b]
                scanHelper(readChild(node, false, seq), seq, a, b, ret);  // traverse its right subtree
            else if (node.key == null || b.compareTo(node.key) < 0)       // node's key is above the upper limit of [a,b]
                scanHelper(readChild(node, true, seq), seq, a, b, ret);   // traverse its left subtree
            else {
                // node is in [a,b] - traverse both of its subtrees
                scanHelper(readChild(node, false, seq), seq, a, b, ret);
                scanHelper(readChild(node, true, seq), seq, a, b, ret);
            }
        }
    }

    /**
        Executes the flag CAS on p for Insert and calls Help to continue the insertion.
        Preconditions: p, l and newInternal are not null, l and newInternal are distinct,
                       newInternal.prevNode = l and if p = root then newInternal.key = null

        @param p            the parent of l (will be the connectorNode)
        @param l            the leaf that will be substituted by newInternal
        @param pinfo        p's info field returned by validateLeaf
        @param linfo        l's info field returned by validateLeaf
        @param newInternal  the Internal node of the new triad
        @param seq          the sequence number of Insert operation
        @return             the result of the last call to help used to continue the insertion,
                            false if any node is frozen or the info flag CAS fails
    */
    private final boolean executeInsert(final Node<K,V> p, final Node<K,V> l, final Info<K,V> pinfo, final Info<K,V> linfo,
                                  final Node<K,V> newInternal, final int seq) {
        // Last check for helping
        if (p.frozen(pinfo)) {
            if (pinfo.state == State.NULL || pinfo.state == State.TRY)
                help(pinfo);
            return false;
        }
        if (l.frozen(linfo)) {
            if (linfo.state == State.NULL || linfo.state == State.TRY)
                help(linfo);
            return false;
        }

        // Optimization - extra handshaking check
        if (counter.get() != seq) return false;

        InfoObjectHolder infoObjectHolder = infoObject.get();
        if (infoObjectHolder.shallCreateNewInfo)    // Create an info object
            infoObjectHolder.info = new Info<K,V>(State.NULL, p, l, linfo, newInternal, seq);
        else    // Refresh existing info object
            infoObjectHolder.refreshInsertInfo(State.NULL, p, l, linfo, newInternal, seq);

        // Perform info flag CAS on p
        if ((p.info == pinfo) && infoUpdater.compareAndSet(p, pinfo, infoObjectHolder.info)) {
            infoObjectHolder.shallCreateNewInfo = true;
            return help(infoObjectHolder.info);
        }
        else
            infoObjectHolder.shallCreateNewInfo = false;
        return false;
    }

    /**
        Executes the flag CAS on gp for Delete and calls Help to continue the deletion.
        Preconditions: gp, p, l, s and newSibling are not null, p and newSibling are distinct,
                       newSibling.prevNode = p and if gp = root then newSibling.key = null

        @param gp           the grandparent of l (will be the connectorNode)
        @param p            the parent of l (will be substituted by newSibling)
        @param l            the leaf that will be deleted
        @param s            the sibling of l (will be deleted)
        @param gpinfo       gp's info field returned by validateLeaf
        @param pinfo        p's info field returned by validateLeaf
        @param linfo        l's info field returned by validateLeaf
        @param sinfo        s's info field returned by validateLink
        @param newSibling   the new copy of s
        @param seq          the sequence number of Delete operation
        @return             the result of the last call to help used to continue the deletion,
                            false if any node is frozen or the info flag CAS fails
    */
    private final boolean executeDelete(final Node<K,V> gp, final Node<K,V> p, final Node<K,V> l, final Node<K,V> s,
                                  final Info<K,V> gpinfo, final Info<K,V> pinfo,
                                  final Info<K,V> linfo, final Info<K,V> sinfo,
                                  final Node<K,V> newSibling, final int seq) {
        // Last check for helping
        if (gp.frozen(gpinfo)) {
            if (gpinfo.state == State.NULL || gpinfo.state == State.TRY)
                help(gpinfo);
            return false;
        }
        if (p.frozen(pinfo)) {
            if (pinfo.state == State.NULL || pinfo.state == State.TRY)
                help(pinfo);
            return false;
        }
        if (l.frozen(linfo)) {
            if (linfo.state == State.NULL || linfo.state == State.TRY)
                help(linfo);
            return false;
        }
        if (s.frozen(sinfo)) {
            if (sinfo.state == State.NULL || sinfo.state == State.TRY)
                help(sinfo);
            return false;
        }

        // Optimization - extra handshaking check
        if (counter.get() != seq) return false;

        InfoObjectHolder infoObjectHolder = infoObject.get();
        if (infoObjectHolder.shallCreateNewInfo)    // Create an info object
            infoObjectHolder.info = new Info<K,V>(State.NULL, gp, p, l, s, pinfo, linfo, sinfo, newSibling, seq);
        else    // Refresh existing info object
            infoObjectHolder.refreshDeleteInfo(State.NULL, gp, p, l, s, pinfo, linfo, sinfo, newSibling, seq);

        // Perform info flag CAS on gp
        if ((gp.info == gpinfo) && infoUpdater.compareAndSet(gp, gpinfo, infoObjectHolder.info)) {
            infoObjectHolder.shallCreateNewInfo = true;
            return help(infoObjectHolder.info);
        }
        else
            infoObjectHolder.shallCreateNewInfo = false;
        return false;
    }

    /**
        Executes the mark and child CAS operations
        Precondition: info is neither null nor dummy

        @param info  the info object created by the operation that is being helped
        @return      true upon successful completion of the operation, otherwise false
    */
    private final boolean help(final Info<K,V> info) {
        // Perform handshaking
        if (info.state == State.NULL) {   // Optimization check
            if (counter.get() != info.handshakingSeq)
                stateUpdater.compareAndSet(info, State.NULL, State.ABORT);
            else
                stateUpdater.compareAndSet(info, State.NULL, State.TRY);
        }

        boolean success = (info.state == State.TRY);
        if (success) {
            // Perform first mark CAS
            if (info.firstMarkedNode.info == info.firstMarkedOldInfo)   // Optimization check
                infoUpdater.compareAndSet(info.firstMarkedNode, info.firstMarkedOldInfo, info);
            success = (info.firstMarkedNode.info == info);
            if (success && info.secondMarkedNode != null) {
                // Perform second mark CAS
                if (info.secondMarkedNode.info == info.secondMarkedOldInfo)   // Optimization check
                    infoUpdater.compareAndSet(info.secondMarkedNode, info.secondMarkedOldInfo, info);
                success = (info.secondMarkedNode.info == info);
                if (success) {
                    // Perform third mark CAS
                    if (info.thirdMarkedNode.info == info.thirdMarkedOldInfo)   // Optimization check
                        infoUpdater.compareAndSet(info.thirdMarkedNode, info.thirdMarkedOldInfo, info);
                    success = (info.thirdMarkedNode.info == info);
                }
            }
        }
        if (success) {
            // Perform child CAS
            if (info.firstMarkedNode == info.connectorNode.leftChild ||
                info.firstMarkedNode == info.connectorNode.rightChild)   // Optimization check
                (info.connectorNode.leftChild == info.firstMarkedNode ?
                 leftChildUpdater : rightChildUpdater).compareAndSet(info.connectorNode, info.firstMarkedNode, info.newNode);
            info.state = State.COMMIT;
        } else if (info.state == State.TRY)
            info.state = State.ABORT;
        return (info.state == State.COMMIT);
    }
}
