package com.pickyberry.internshipassignment.presentation

import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pickyberry.internshipassignment.domain.SortTypes
import com.pickyberry.internshipassignment.data.RepositoryImpl
import com.pickyberry.internshipassignment.domain.FileItem
import com.pickyberry.internshipassignment.domain.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.Collections

class FileListViewModel : ViewModel() {

    private val repository: Repository = RepositoryImpl()
    private val _currentFiles = MutableLiveData<List<FileItem>>()
    val currentFiles = _currentFiles
    var sortedBy = SortTypes.NAMES_ASC
    val loading = MutableLiveData<Boolean>()
    var showingUpdatedFiles = false
    private var allFiles = listOf<FileItem>()
    private var updatedFiles = listOf<FileItem>()


    init {
        getFiles(Environment.getExternalStorageDirectory().absolutePath,SortTypes.NAMES_ASC)
    }

    fun getFiles(rootPath: String, newType: SortTypes?) {
        val sortType = newType ?: sortedBy
        viewModelScope.launch(Dispatchers.IO) {
            loading.postValue(true)

                val list = mutableListOf<FileItem>()

                if (showingUpdatedFiles)
                    repository.getUpdatedFiles()
                else
                    repository.getFiles(
                        File(Environment.getExternalStorageDirectory().absolutePath),
                        list
                    )
                sort(list,sortType)
                _currentFiles.postValue(list)
                sortedBy = sortType


            loading.postValue(false)
        }
    }

    fun sort(list: MutableList<FileItem>,type: SortTypes){
        if (shouldReverse(type)) list.reverse()
        else when (type) {
            //ПОПРОБОВАТЬ ЗАМЕНИТЬ НА list.sortBy !!!!!!!!!!
            SortTypes.NAMES_ASC -> Collections.sort(list, FileItem.sortNamesAscending())
            SortTypes.NAMES_DESC -> Collections.sort(list, FileItem.sortNamesDescending())
            SortTypes.SIZE_ASC -> Collections.sort(list, FileItem.sortSizesAscending())
            SortTypes.SIZE_DESC -> Collections.sort(list, FileItem.sortSizesDescending())
            SortTypes.DATE_ASC -> Collections.sort(list, FileItem.sortDatesAscending())
            SortTypes.DATE_DESC -> Collections.sort(list, FileItem.sortDatesDescending())
            SortTypes.EXT_ASC -> Collections.sort(list, FileItem.sortExtensionsAscending())
            SortTypes.EXT_DESC -> Collections.sort(
                list,
                FileItem.sortExtensionsDescending()
            )
        }
        _currentFiles.postValue(list)
        sortedBy = type
    }

    fun switchBetweenAllAndUpdated() {
        showingUpdatedFiles = !showingUpdatedFiles
        if (showingUpdatedFiles) allFiles = currentFiles.value!!
        else updatedFiles = currentFiles.value!!
      //  getFiles(sortedBy)
    }

    private fun shouldReverse(type: SortTypes): Boolean {
        return ((type == SortTypes.NAMES_ASC && sortedBy == SortTypes.NAMES_DESC) ||
                (type == SortTypes.NAMES_DESC && sortedBy == SortTypes.NAMES_ASC) ||
                (type == SortTypes.SIZE_ASC && sortedBy == SortTypes.SIZE_DESC) ||
                (type == SortTypes.SIZE_DESC && sortedBy == SortTypes.SIZE_ASC) ||
                (type == SortTypes.DATE_ASC && sortedBy == SortTypes.DATE_DESC) ||
                (type == SortTypes.DATE_DESC && sortedBy == SortTypes.DATE_ASC) ||
                (type == SortTypes.EXT_ASC && sortedBy == SortTypes.EXT_DESC) ||
                (type == SortTypes.EXT_DESC && sortedBy == SortTypes.EXT_ASC))
    }

}