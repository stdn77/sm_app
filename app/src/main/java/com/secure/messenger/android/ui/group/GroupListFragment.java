package com.secure.messenger.android.ui.group;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.secure.messenger.android.R;
import com.secure.messenger.android.data.model.Group;
import com.secure.messenger.android.ui.common.adapter.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Фрагмент для відображення списку груп
 */
public class GroupListFragment extends Fragment {

    private GroupViewModel viewModel;
    private RecyclerView recyclerView;
    private GroupAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ініціалізація ViewModel
        viewModel = new ViewModelProvider(this).get(GroupViewModel.class);

        // Ініціалізація UI компонентів
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();

        // Спостереження за змінами даних
        observeViewModel();

        // Завантаження даних
        viewModel.loadGroups();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        emptyView = view.findViewById(R.id.empty_view);
        fab = view.findViewById(R.id.fab);
    }

    private void setupRecyclerView() {
        adapter = new GroupAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        // Налаштування обробника натиснень
        adapter.setOnItemClickListener((group, position, view) -> {
            // Відкриття екрану групи
            openGroupChat(group);
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshGroups();
        });
    }

    private void setupFab() {
        fab.setOnClickListener(v -> {
            // Відкриття екрану створення нової групи
            openCreateGroupScreen();
        });
    }

    private void observeViewModel() {
        // Спостереження за списком груп
        viewModel.getGroups().observe(getViewLifecycleOwner(), groups -> {
            adapter.setItems(groups);
            updateEmptyView(groups.isEmpty());
        });

        // Спостереження за станом завантаження
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });

        // Спостереження за помилками
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.errorHandled();
            }
        });
    }

    private void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void openGroupChat(Group group) {
        Intent intent = new Intent(requireContext(), GroupChatActivity.class);
        intent.putExtra(GroupChatActivity.EXTRA_GROUP_ID, group.getId());
        intent.putExtra(GroupChatActivity.EXTRA_GROUP_NAME, group.getName());
        startActivity(intent);
    }

    private void openCreateGroupScreen() {
        Intent intent = new Intent(requireContext(), CreateGroupActivity.class);
        startActivity(intent);
    }

    /**
     * Адаптер для списку груп
     */
    private static class GroupAdapter extends BaseAdapter<Group, GroupAdapter.GroupViewHolder> {

        @NonNull
        @Override
        public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflate(parent, R.layout.item_group);
            return new GroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
            Group group = getItem(position);
            if (group == null) return;

            // Налаштування даних для відображення
            holder.bind(group);

            // Налаштування обробників подій
            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(group, position, v);
                }
            });

            holder.itemView.setOnLongClickListener(v -> {
                if (onItemLongClickListener != null) {
                    return onItemLongClickListener.onItemLongClick(group, position, v);
                }
                return false;
            });
        }

        @Override
        protected boolean areItemsTheSame(Group oldItem, Group newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        protected boolean areContentsTheSame(Group oldItem, Group newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getMemberCount() == newItem.getMemberCount();
        }

        /**
         * ViewHolder для елемента групи
         */
        static class GroupViewHolder extends RecyclerView.ViewHolder {
            private TextView textName, textDescription, textMemberCount, textReportStatus;

            public GroupViewHolder(@NonNull View itemView) {
                super(itemView);
                textName = itemView.findViewById(R.id.text_group_name);
                textDescription = itemView.findViewById(R.id.text_group_description);
                textMemberCount = itemView.findViewById(R.id.text_member_count);
                textReportStatus = itemView.findViewById(R.id.text_report_status);
            }

            public void bind(Group group) {
                textName.setText(group.getName());
                textDescription.setText(group.getDescription());
                textMemberCount.setText(String.format("%d учасників", group.getMemberCount()));

                if (group.isReportEnabled()) {
                    textReportStatus.setVisibility(View.VISIBLE);
                    textReportStatus.setText("Звіти активовані");
                } else {
                    textReportStatus.setVisibility(View.GONE);
                }
            }
        }
    }
}