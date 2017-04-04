package net.seiko_comb.onkohdondo.util;

import java.util.Optional;

public class ConcurrentLoopableQueue<T> {
	private int capacity;
	private Object[] t;
	private int begin, end;

	public ConcurrentLoopableQueue(int capacity) {
		this.capacity = capacity;
		t = new Object[capacity];
		begin = end = 0;
	}

	public int size() {
		return end - begin;
	}

	public synchronized void addLast(T element) {
		t[end++ % capacity] = (Object) element;
		if (begin % capacity == end % capacity) {
//			throw new OutOfCapacityException();
			begin++;
		}
	}

	public synchronized boolean removeFirst() {
		if (size() == 0) return false;
		t[begin++ % capacity] = null;
		return true;
	}

	@SuppressWarnings("unchecked")
	public synchronized T get(int index) {
		if (index < 0 || size() <= index) throw new IndexOutOfBoundsException();
		return (T) t[(begin + index) % capacity];
	}

	public Pointer begin() {
		return getPointer(0);
	}

	public Pointer end() {
		return getPointer(size());
	}

	public Pointer getPointer(int index) {
		if (index < 0 || size() < index) throw new IndexOutOfBoundsException();
		return new Pointer(begin + index);
	}

	public class Pointer {
		private int index;

		public Pointer(int index) {
			this.index = index;
		}

		public boolean isBegin() {
			return index == begin;
		}

		public boolean isEnd() {
			return index == end;
		}

		public Optional<T> forward() {
			synchronized (ConcurrentLoopableQueue.this) {
				index++;
				return getAtIndex();
			}
		}

		public Optional<T> backward() {
			synchronized (ConcurrentLoopableQueue.this) {
				index--;
				return getAtIndex();
			}
		}

		private Optional<T> getAtIndex() {
			if (begin <= index && index < end) {
				return Optional.of(get(index - begin));
			} else return Optional.empty();
		}

		public int getIndex() {
			return index - begin;
		}

		public void removeFromHeadThroughHere() {
			synchronized (ConcurrentLoopableQueue.this) {
				begin = Math.max(index, begin);
			}
		}
	}

	public static class OutOfCapacityException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}
}
