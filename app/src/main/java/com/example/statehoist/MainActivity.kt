package com.example.statehoist

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.statehoist.ui.theme.StateHoistTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StateHoistTheme {
                val viewModel by viewModels<MainViewModel>()
                val searchText by viewModel.searchText.collectAsState()
                val cathegory by viewModel.category.collectAsState()
                val isSearching by viewModel.isSearching.collectAsState()
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Search",
                        fontSize = 25.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(50.dp))
                    TextField(
                        value = searchText,
                        onValueChange = { viewModel.onSearchTextChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp)),
                        colors = TextFieldDefaults.textFieldColors(containerColor = Color.White),
                        placeholder = {
                            Text(
                                text = "Search course,topic,mentor.",
                                fontSize = 23.sp,
                                color = Color.LightGray
                            )
                        },
                        singleLine = true,
                        trailingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_sort_24),
                                contentDescription = "",
                                tint = Color.LightGray,
                                modifier = Modifier.size(30.dp)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "",
                                tint = Color.Black,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isSearching) {
                        Box(modifier = Modifier.fillMaxSize()) {
                        }
                    } else if (cathegory.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "No Cathegories found", fontSize = 25.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.height(50.dp))
                        LazyVerticalGrid(
                            modifier = Modifier,
                            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                            columns = GridCells.Adaptive(150.dp)
                        ) {
                            items(cathegory.size) { index ->
                                CourseSearchItemView(course = cathegory[index]) { str ->
                                    Log.d("Data", str)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

@Composable
fun CourseSearchItemView(course: CourseCategory, onTap:(String) -> Unit) {
        Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 120.dp)
                    .clip(RoundedCornerShape(20.dp)).clickable { onTap(course.id) }
                    .background(color = Color.Magenta, shape = RoundedCornerShape(size = 20.dp)),
               Arrangement.Center,Alignment.CenterHorizontally
            ) {
                Text(
                    text = course.title,
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(text = "${course.noClasses} classes",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W700)
            }
     }
}



class MainViewModel: ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _cathegory = MutableStateFlow(cathegories)
    val category = searchText
        .debounce(1000L)
        .onEach { _isSearching.update { true } }
        .combine(_cathegory) { text, cathegory ->
            if(text.isBlank()) {
                cathegory
            } else {
                delay(1000L)
                cathegory.filter {
                    it.doesMatchSearchQuery(text)
                }
            }
        }
        .onEach { _isSearching.update { false } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(2000),
            _cathegory.value
        )

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }
}



private val cathegories = listOf(
   CourseCategory("1","IT TECH",1103 ,R.drawable.baseline_history_24.toString()),
    CourseCategory("2","Marketing",1203,R.drawable.baseline_history_24.toString()),
    CourseCategory("3","Accounting",1033,R.drawable.baseline_history_24.toString()),
    CourseCategory("4","3D Animation",1203,R.drawable.baseline_history_24.toString()),
    CourseCategory("5","Analyst",983,R.drawable.baseline_history_24.toString()),
    CourseCategory("6","Enginnering",1213,R.drawable.baseline_history_24.toString())
)


data class CourseCategory(
    val id: String,
    val title: String,
    val noClasses: Int,
    val image: String
){
    fun doesMatchSearchQuery(query: String): Boolean {
        val matchingCombinations = listOf(
            title,
            "$noClasses"
        )

        return matchingCombinations.any {
            it.contains(query, ignoreCase = true)
        }
    }
}




