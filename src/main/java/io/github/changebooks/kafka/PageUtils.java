package io.github.changebooks.kafka;

import org.springframework.util.Assert;

import java.util.AbstractList;
import java.util.List;

/**
 * 分页
 *
 * @author changebooks@qq.com
 */
public final class PageUtils {
    /**
     * 计算分页
     *
     * @param list 列表
     * @param size 总页数
     * @param <T>  元素类型
     * @return 分页列表
     */
    public static <T> List<List<T>> compute(List<T> list, int size) {
        Assert.notNull(list, "list can't be null");
        Assert.isTrue(size > 0, "size must be greater than 0");

        int pageSize = (int) Math.ceil((double) list.size() / size);
        pageSize = Math.max(pageSize, 1);

        return new PageList<>(list, pageSize);
    }

    /**
     * 计算分页
     *
     * @param list     列表
     * @param pageSize 每页行数
     * @param <T>      元素类型
     * @return 分页列表
     */
    public static <T> List<List<T>> page(List<T> list, int pageSize) {
        Assert.notNull(list, "list can't be null");
        Assert.isTrue(pageSize > 0, "pageSize must be greater than 0");

        return new PageList<>(list, pageSize);
    }

    /**
     * 分页列表
     *
     * @param <T> 元素类型
     */
    private static class PageList<T> extends AbstractList<List<T>> {
        /**
         * 列表
         */
        final List<T> list;

        /**
         * 每页行数
         */
        final int pageSize;

        /**
         * 总行数
         */
        final int totalSize;

        /**
         * 总页数
         */
        final int size;

        PageList(List<T> list, int pageSize) {
            this.list = list;
            this.pageSize = pageSize;
            this.totalSize = list.size();
            this.size = (int) Math.ceil((double) totalSize / pageSize);
        }

        @Override
        public List<T> get(int index) {
            rangeCheck(index);

            int start = index * pageSize;
            int end = Math.min(start + pageSize, totalSize);
            return list.subList(start, end);
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return list.isEmpty();
        }

        /**
         * 下标越界？
         *
         * @param index 下标
         */
        private void rangeCheck(int index) {
            if (index < 0 || index >= size) {
                throw new IndexOutOfBoundsException("index range is [0, " + size + ")");
            }
        }

    }

}
