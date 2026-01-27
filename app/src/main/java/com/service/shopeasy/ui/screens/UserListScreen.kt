package com.service.shopeasy.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.layout.LazyLayoutCacheWindow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.service.shopeasy.ui.components.UserItem
import com.service.shopeasy.ui.viewmodel.UsersIntent
import com.service.shopeasy.ui.viewmodel.UsersViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserListScreen(usersListViewModel: UsersViewModel = hiltViewModel()){
    /*
    * For better performance.
    * https://www.linkedin.com/posts/ibrahim-asgari_performanceoptimization-jetpackcompose-androiddevelopment-share-7421845377026494464-d229?utm_source=share&utm_medium=member_desktop&rcm=ACoAAA0h4zkBHeoH2TunSUAjl_H-BJ9b0AhLYeI
    * */
    val state by usersListViewModel.state.collectAsStateWithLifecycle()

    /*
    * based on the this post link information thought to change add LazyLayoutCacheWindow to improve performance
    * https://www.linkedin.com/feed/update/urn:li:activity:7416115907233480704?updateEntityUrn=urn%3Ali%3Afs_updateV2%3A%28urn%3Ali%3Aactivity%3A7416115907233480704%2CFEED_DETAIL%2CEMPTY%2CDEFAULT%2Cfalse%29
    * */
    val cacheWindow = remember {
        LazyLayoutCacheWindow(
            ahead = 240.dp,
            behind = 120.dp)
    }

    val lazyState = rememberLazyListState(cacheWindow = cacheWindow)

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
            LazyColumn(state = lazyState) {
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