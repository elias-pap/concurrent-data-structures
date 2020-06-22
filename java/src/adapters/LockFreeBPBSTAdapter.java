/**
 * Java test harness for throughput experiments on concurrent data structures.
 * Copyright (C) 2012 Trevor Brown
 * Contact (me [at] tbrown [dot] pro) with any questions or comments.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package adapters;

import algorithms.published.LockFreeBPBSTMap;
import main.support.SetInterface;
import main.support.KSTNode;
import main.support.OperationListener;
import main.support.Random;

/**
 *
 * @author elias-pap
 */
public class LockFreeBPBSTAdapter<K extends Comparable<? super K>> extends AbstractAdapter<K> implements SetInterface<K> {
    LockFreeBPBSTMap<K,K> tree;

    public LockFreeBPBSTAdapter(int k) {
        tree = new LockFreeBPBSTMap<K,K>(k);
    }

    public LockFreeBPBSTAdapter() {
        tree = new LockFreeBPBSTMap<K,K>();
    }

    public boolean contains(K key) {
        return tree.containsKey(key);
    }

    @Override
    public boolean add(K key, Random rng, final int[] metrics) {
        return tree.putIfAbsent(key, key) == null;
//        return tree.put(key, key) == null;
    }

    public boolean add(K key, Random rng) {
        return add(key, rng, null);
    }

    public K get(K key) {
        //return tree.get(key);
		return null;
    }

    @Override
    public boolean remove(K key, Random rng, final int[] metrics) {
        return tree.remove(key) != null;
    }

    public boolean remove(K key, Random rng) {
        return remove(key, rng, null);
    }

    @Override
    public Object rangeQuery(K lo, K hi, int rangeSize, Random rng) {
        return tree.rangeScan(lo, hi);
    }

    public void addListener(OperationListener l) {}

    public int size() {
        return sequentialSize();
    }

    public KSTNode<K> getRoot() {
        return null;
    }

    public long getSumOfKeys() {
        return tree.getSumOfKeys();
    }

    public boolean supportsKeysum() {
        return true;
    }

    public long getKeysum() {
        return tree.getSumOfKeys();
    }

    public int getSumOfDepths() {
        return tree.getSumOfDepths();
    }

    public int sequentialSize() {
        return tree.size();
    }

}
