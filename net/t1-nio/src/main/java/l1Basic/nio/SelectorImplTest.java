package l1Basic.nio;

import io.netty.util.internal.PlatformDependent;
import io.netty.util.internal.ReflectionUtil;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;

/**
 * @author: zxz
 * @date: 2020/7/24
 */
public class SelectorImplTest {
    public static void main(String[] args) {
        Object maybeSelectorImplClass = AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                System.out.println("aaaaaaaaaaaaaaaa");
                try {
                    return Class.forName(
                            "sun.nio.ch.SelectorImpl",
                            false,
                            PlatformDependent.getSystemClassLoader());
                } catch (Throwable cause) {
                    return cause;
                }
            }
        });
        System.out.println(maybeSelectorImplClass);
        final Selector unwrappedSelector;
        try {
            unwrappedSelector = SelectorProvider.provider().openSelector();
            final Class<?> selectorImplClass = (Class<?>) maybeSelectorImplClass;
            final SelectedSelectionKeySet selectionKeys=new SelectedSelectionKeySet();
            Object maybeException = AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    try {
                        Field selectedKeysField = selectorImplClass.getDeclaredField("selectedKeys");
                        Field publicSelectedKeysField = selectorImplClass.getDeclaredField("publicSelectedKeys");
                        Throwable cause = ReflectionUtil.trySetAccessible(selectedKeysField, true);
                        if (cause != null) {
                            return cause;
                        }
                        cause = ReflectionUtil.trySetAccessible(publicSelectedKeysField, true);
                        if (cause != null) {
                            return cause;
                        }
                        selectedKeysField.set(unwrappedSelector, selectionKeys);
                        publicSelectedKeysField.set(unwrappedSelector, selectionKeys);
                        return null;
                    } catch (NoSuchFieldException e) {
                        return e;
                    } catch (IllegalAccessException e) {
                        return e;
                    }
                }
            });
            Set<SelectionKey> set = unwrappedSelector.selectedKeys();
            System.out.println(set.getClass());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static final class SelectedSelectionKeySet extends AbstractSet<SelectionKey> {
        SelectionKey[] keys;
        int size;

        SelectedSelectionKeySet() {
            keys = new SelectionKey[1024];
        }

        @Override
        public boolean add(SelectionKey o) {
            if (o == null) {
                return false;
            }

            keys[size++] = o;
            if (size == keys.length) {
                increaseCapacity();
            }

            return true;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public Iterator<SelectionKey> iterator() {
            return new Iterator<SelectionKey>() {
                private int idx;

                public boolean hasNext() {
                    return idx < size;
                }

                public SelectionKey next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return keys[idx++];
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        void reset() {
            reset(0);
        }

        void reset(int start) {
            Arrays.fill(keys, start, size, null);
            size = 0;
        }

        private void increaseCapacity() {
            SelectionKey[] newKeys = new SelectionKey[keys.length << 1];
            System.arraycopy(keys, 0, newKeys, 0, size);
            keys = newKeys;
        }
    }

}
