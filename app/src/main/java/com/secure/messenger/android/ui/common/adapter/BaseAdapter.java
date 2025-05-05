package com.secure.messenger.android.ui.common.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.secure.messenger.android.data.local.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Базовий адаптер для RecyclerView з підтримкою DiffUtil
 *
 * @param <T> тип даних, що відображаються
 * @param <VH> тип ViewHolder
 */
public abstract class BaseAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    protected List<T> items = new ArrayList<>();
    protected OnItemClickListener<T> onItemClickListener;
    protected OnItemLongClickListener<T> onItemLongClickListener;
    protected PreferenceManager preferenceManager;

    /**
     * Встановлює слухача кліків по елементах
     *
     * @param listener слухач кліків
     */
    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.onItemClickListener = listener;
    }

    /**
     * Встановлює слухача довгих кліків по елементах
     *
     * @param listener слухач довгих кліків
     */
    public void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        this.onItemLongClickListener = listener;
    }

    /**
     * Створення ViewHolder
     *
     * @param parent батьківський ViewGroup
     * @param viewType тип View
     * @return створений ViewHolder
     */
    @NonNull
    @Override
    public abstract VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType);

    /**
     * Заповнення ViewHolder даними
     *
     * @param holder ViewHolder для заповнення
     * @param position позиція елемента
     */
    @Override
    public abstract void onBindViewHolder(@NonNull VH holder, int position);

    /**
     * Отримання кількості елементів
     *
     * @return кількість елементів
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Отримання елемента за позицією
     *
     * @param position позиція елемента
     * @return елемент за вказаною позицією
     */
    public T getItem(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    /**
     * Встановлення нового списку елементів з використанням DiffUtil
     *
     * @param newItems новий список елементів
     */
    public void setItems(List<T> newItems) {
        if (newItems == null) {
            newItems = new ArrayList<>();
        }

        DiffUtilCallback<T> diffCallback = new DiffUtilCallback<>(items, newItems, this::areItemsTheSame, this::areContentsTheSame);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        items.clear();
        items.addAll(newItems);

        diffResult.dispatchUpdatesTo(this);
    }

    /**
     * Додавання нового елементу в кінець списку
     *
     * @param item новий елемент
     */
    public void addItem(T item) {
        if (item == null) {
            return;
        }

        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    /**
     * Додавання нового елементу за вказаною позицією
     *
     * @param position позиція для вставки
     * @param item новий елемент
     */
    public void addItem(int position, T item) {
        if (item == null || position < 0 || position > items.size()) {
            return;
        }

        items.add(position, item);
        notifyItemInserted(position);
    }

    /**
     * Оновлення елементу за вказаною позицією
     *
     * @param position позиція для оновлення
     * @param item новий елемент
     */
    public void updateItem(int position, T item) {
        if (item == null || position < 0 || position >= items.size()) {
            return;
        }

        items.set(position, item);
        notifyItemChanged(position);
    }

    /**
     * Видалення елементу за вказаною позицією
     *
     * @param position позиція для видалення
     */
    public void removeItem(int position) {
        if (position < 0 || position >= items.size()) {
            return;
        }

        items.remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Видалення всіх елементів
     */
    public void clearItems() {
        int size = items.size();
        items.clear();
        notifyItemRangeRemoved(0, size);
    }

    /**
     * Перевірка, чи два елементи представляють один і той самий об'єкт
     *
     * @param oldItem старий елемент
     * @param newItem новий елемент
     * @return true, якщо елементи представляють один і той самий об'єкт
     */
    protected abstract boolean areItemsTheSame(T oldItem, T newItem);

    /**
     * Перевірка, чи зміст двох елементів однаковий
     *
     * @param oldItem старий елемент
     * @param newItem новий елемент
     * @return true, якщо зміст елементів однаковий
     */
    protected abstract boolean areContentsTheSame(T oldItem, T newItem);

    /**
     * Інтерфейс для обробки кліків по елементу
     *
     * @param <T> тип елемента
     */
    public interface OnItemClickListener<T> {
        void onItemClick(T item, int position, View view);
    }

    /**
     * Інтерфейс для обробки довгих кліків по елементу
     *
     * @param <T> тип елемента
     */
    public interface OnItemLongClickListener<T> {
        boolean onItemLongClick(T item, int position, View view);
    }

    /**
     * Допоміжний клас для порівняння елементів у DiffUtil
     *
     * @param <T> тип елемента
     */
    private static class DiffUtilCallback<T> extends DiffUtil.Callback {
        private final List<T> oldList;
        private final List<T> newList;
        private final ItemComparator<T> itemsComparator;
        private final ItemComparator<T> contentsComparator;

        /**
         * Інтерфейс для порівняння елементів
         *
         * @param <T> тип елемента
         */
        interface ItemComparator<T> {
            boolean compare(T oldItem, T newItem);
        }

        /**
         * Конструктор
         *
         * @param oldList старий список
         * @param newList новий список
         * @param itemsComparator порівнювач ідентичності елементів
         * @param contentsComparator порівнювач вмісту елементів
         */
        DiffUtilCallback(List<T> oldList, List<T> newList, ItemComparator<T> itemsComparator, ItemComparator<T> contentsComparator) {
            this.oldList = oldList;
            this.newList = newList;
            this.itemsComparator = itemsComparator;
            this.contentsComparator = contentsComparator;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return itemsComparator.compare(oldList.get(oldItemPosition), newList.get(newItemPosition));
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            return contentsComparator.compare(oldList.get(oldItemPosition), newList.get(newItemPosition));
        }
    }

    /**
     * Утилітарний метод для створення View з layout-ресурсу
     *
     * @param parent батьківський ViewGroup
     * @param layoutId ідентифікатор layout-ресурсу
     * @return створений View
     */
    protected View inflate(@NonNull ViewGroup parent, int layoutId) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
    }
}