package com.service.shopeasy.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.service.shopeasy.ui.components.UserItem
import com.service.shopeasy.ui.viewmodel.UsersIntent
import com.service.shopeasy.ui.viewmodel.UsersViewModel

@Composable
fun UserListScreen(usersListViewModel: UsersViewModel = hiltViewModel()){
    val state by usersListViewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        usersListViewModel.onUserIntent(UsersIntent.LoadUsers)
    }

    when {
        state.loading -> {
            CenteredLoading()
        }
        state.error != null -> {
            ErrorWithRetry(state.error!!) {
                usersListViewModel.onUserIntent(UsersIntent.Refresh)
            }
        }
        else -> {
            LazyColumn {
                items(
                    items = state.users,
                    key = { user -> user.id }
                ) { user ->
                    println(user.name)
                    UserItem(user = user, modifier = Modifier)
                }
            }
        }
    }
}