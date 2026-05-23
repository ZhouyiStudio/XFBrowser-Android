package com.xuanfeng.browser;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 书签管理器 - 使用 SharedPreferences 存储书签
 */
public class BookmarkManager {
    private static final String PREFS_NAME = "bookmarks";
    private static final String KEY_BOOKMARKS = "bookmark_list";
    private static final String SEPARATOR = "|||";

    private static BookmarkManager instance;
    private final SharedPreferences prefs;

    private BookmarkManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized BookmarkManager getInstance(Context context) {
        if (instance == null) {
            instance = new BookmarkManager(context.getApplicationContext());
        }
        return instance;
    }

    /** 获取所有书签 */
    public List<BookmarkItem> getBookmarks() {
        Set<String> raw = prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>());
        List<BookmarkItem> list = new ArrayList<>();
        for (String s : raw) {
            BookmarkItem item = BookmarkItem.fromString(s);
            if (item != null) list.add(item);
        }
        return list;
    }

    /** 添加书签 */
    public void addBookmark(String title, String url) {
        Set<String> raw = new HashSet<>(prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>()));
        raw.add(title + SEPARATOR + url);
        prefs.edit().putStringSet(KEY_BOOKMARKS, raw).apply();
    }

    /** 删除书签 */
    public void removeBookmark(String title, String url) {
        Set<String> raw = new HashSet<>(prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>()));
        raw.remove(title + SEPARATOR + url);
        prefs.edit().putStringSet(KEY_BOOKMARKS, raw).apply();
    }

    /** 检查是否已收藏 */
    public boolean isBookmarked(String url) {
        for (BookmarkItem item : getBookmarks()) {
            if (item.url.equals(url)) return true;
        }
        return false;
    }

    /** 删除指定 URL 的书签 */
    public void removeByUrl(String url) {
        Set<String> raw = new HashSet<>(prefs.getStringSet(KEY_BOOKMARKS, new HashSet<>()));
        Set<String> toRemove = new HashSet<>();
        for (String s : raw) {
            BookmarkItem item = BookmarkItem.fromString(s);
            if (item != null && item.url.equals(url)) {
                toRemove.add(s);
            }
        }
        raw.removeAll(toRemove);
        prefs.edit().putStringSet(KEY_BOOKMARKS, raw).apply();
    }

    public static class BookmarkItem {
        public String title;
        public String url;

        public BookmarkItem(String title, String url) {
            this.title = title;
            this.url = url;
        }

        static BookmarkItem fromString(String s) {
            int idx = s.lastIndexOf(SEPARATOR);
            if (idx == -1) return null;
            return new BookmarkItem(s.substring(0, idx), s.substring(idx + SEPARATOR.length()));
        }

        @Override
        public String toString() {
            return title + SEPARATOR + url;
        }
    }
}
