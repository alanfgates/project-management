package com.github.alanfgates.project.management;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class SortedLinkedTree<E> implements Iterable<E> {

  private final Comparator<E> comparator;
  private final LinkedList<E> list;
  private final SortedMap<E, Integer> positions;

  public SortedLinkedTree(Comparator<E> comparator) {
    this.comparator = comparator;
    list = new LinkedList<>();
    positions = new TreeMap<>();
  }

  public int size() {
    return list.size();
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public boolean contains(E e) {
    return positions.containsKey(e);
  }

  public Iterator<E> iterator() {
    return list.iterator();
  }

  public void add(E e) {
    Iterator<Map.Entry<E, Integer>> iter = positions.tailMap(e).entrySet().iterator();
    if (iter.hasNext()) {
      int pos = iter.next().getValue();
      list.add(pos, e);
      rebuildPositions(pos);
    } else {
      list.addLast(e);
      rebuildPositions(list.size() - 1);
    }
  }

  public boolean remove(E e) {
    Integer pos = positions.remove(e);
    if (pos != null) {
      list.remove(pos);
      rebuildPositions(pos);
      return true;
    } else {
      return false;
    }
  }

  public boolean addAll(Collection<? extends E> c) {
    // it's faster to throw them all in and resort then to insert them sorted one at a time
    list.addAll(c);
    list.sort(comparator);
    rebuildPositions(0);
    return true;
  }

  public void clear() {
    list.clear();
    positions.clear();
  }

  public E next(E e) {
    Integer pos = positions.get(e);
    if (pos == null || pos == (list.size() - 1)) return null;
    else return list.get(pos + 1);
  }

  public E prev(E e) {
    Integer pos = positions.get(e);
    if (pos == null || pos == 0) return null;
    else return list.get(pos - 1);
  }

  public E last() {
    return list.getLast();
  }

  public E first() {
    return list.getFirst();
  }

  public Collection<E> asCollection() {
    return new ArrayList<>(list);
  }

  private void rebuildPositions(int from) {
    for (int i = from; i < list.size(); i++) {
      positions.remove(list.get(i));
      positions.put(list.get(i), i);
    }
  }

}
