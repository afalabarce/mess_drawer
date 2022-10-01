package io.github.afalabarce.compose.desktop

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.WindowPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File

private data class FileChooserUiState(
    val currentPath: File,
    val firstLoad: Boolean = true,
    val fileSelection: Boolean,
    val selectedFileOrDirectory: File?
)

private class FileChooserViewModel: CoroutineScope by CoroutineScope(Dispatchers.IO){
    private val _uiState by lazy { MutableStateFlow(FileChooserUiState(
        currentPath = File(System.getProperty("user.dir")),
        firstLoad = true,
        fileSelection = true,
        selectedFileOrDirectory = null
    )) }
    val uiState: StateFlow<FileChooserUiState>
        get() = this._uiState

    fun setSelectionType(onlyDirectories: Boolean){
        this._uiState.update { old -> old.copy(fileSelection = !onlyDirectories) }
    }

    fun setNewFolder(folder: File){
        if (folder.isDirectory){
            this._uiState.update { old -> old.copy(currentPath = folder, firstLoad = false) }
        }
    }

    fun selectFileOrDirectory(selected: File?){
        this._uiState.update { old -> old.copy(selectedFileOrDirectory = selected) }
    }
}

@Composable
fun FileChooserDialog(
    visible: Boolean,
    onlyDirectories: Boolean,
    baseDirectory: File? = null,
    backgroundColor: Color,
    borderColor: Color,
    iconImage: Painter,
    title: String,
    acceptTitle: String = "Aceptar",
    acceptButtonColor: Color,
    cancelTitle: String = "Cancelar",
    cancelButtonColor: Color,
    dotDotColor: Color,
    onFileChoosen: (File?, Boolean) -> Unit
) {
    val viewModel = remember { FileChooserViewModel() }
    viewModel.setSelectionType(onlyDirectories)
    val uiState by viewModel.uiState.collectAsState()

    if (baseDirectory != null && uiState.firstLoad)
        viewModel.setNewFolder(baseDirectory)

    Dialog(
        onCloseRequest = { },
        state = DialogState(position = WindowPosition(Alignment.Center), size = DpSize(800.dp, 600.dp)),
        visible = visible,
        resizable = true,
        undecorated = true,
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(CornerSize(10.dp)),
            elevation = 6.dp,
            border = BorderStroke(width = 2.dp, borderColor),
            backgroundColor = backgroundColor,
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(backgroundColor = borderColor) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(6.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(iconImage, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = title,
                                modifier = Modifier.fillMaxWidth(0.9f),
                                color = Color.White,
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                IconButton(
                                    modifier = Modifier,
                                    onClick = { onFileChoosen(null, false) },
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = null,
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    }
                },
                floatingActionButtonPosition = FabPosition.End,
                isFloatingActionButtonDocked = true,
                floatingActionButton = {
                    Row(modifier = Modifier.padding(end = 4.dp)) {
                        FloatingActionButton(
                            onClick = {
                                if (onlyDirectories){
                                    onFileChoosen(uiState.currentPath, true)
                                }else {
                                    if (uiState.selectedFileOrDirectory != null) {
                                        onFileChoosen(uiState.selectedFileOrDirectory, true)
                                    }
                                }
                            },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(CornerSize(10.dp)),
                            backgroundColor = acceptButtonColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.Done, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = acceptTitle, color = Color.White)
                            }

                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FloatingActionButton(
                            onClick = {
                                onFileChoosen(null, false)
                            },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(CornerSize(10.dp)),
                            backgroundColor = cancelButtonColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = cancelTitle, color = Color.White)
                            }

                        }
                    }
                },
                bottomBar = {
                    BottomAppBar(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp, 0.dp, 0.dp, 0.dp)),
                        backgroundColor = borderColor,
                        cutoutShape = RoundedCornerShape(CornerSize(10.dp))
                    ) { }
                }

            ) {
                Column(modifier = Modifier.fillMaxSize().padding(start = 6.dp, end = 6.dp, bottom = 64.dp)) {
                    PathContainer(path = uiState.currentPath, color = borderColor, dotDotColor = dotDotColor) { newFolder ->
                        viewModel.setNewFolder(newFolder)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.currentPath.listFiles()?.filter { x -> (onlyDirectories && x.isDirectory) || !onlyDirectories }?.toTypedArray() ?: arrayOf()) { file ->
                            FileItem(file, file == uiState.selectedFileOrDirectory) {
                                if (file.isDirectory){
                                    viewModel.setNewFolder(file)
                                }else{
                                    viewModel.selectFileOrDirectory(file)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileItem(file: File, isSelected: Boolean = false, onClick: (File) -> Unit){
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick(file) }.clip(MaterialTheme.shapes.small).background(if(isSelected) Color(0xFF989898) else Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = if (file.isDirectory) Icons.Filled.Folder else Icons.Filled.FilePresent,
            contentDescription = null,
            tint = if (file.isDirectory) Color(0xFFFFBF00) else Color.Gray
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = file.name,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PathContainer(path: File, dotDotColor: Color, color: Color, clickedPath: (File) -> Unit){
    val dotDot: String = ".."
    val parentFolder: String = try {
        if (File(path.parent).name == "") File(path.parent).absolutePath else File(path.parent).name
    }catch (ex: Exception){
        path.absolutePath
    }
    val currentPath: String = path.name

    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ){
        IconButton(
            onClick = {
                clickedPath(File(System.getProperty("user.dir")))
            }
        ){
            Icon(Icons.Filled.Home, contentDescription = null, tint = color)
        }
        Text(
            text = "Ruta actual:",
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold
        )

        ContentTag(
            isLeft = false,
            color = dotDotColor,
            text = dotDot
        ){
            if (currentPath != "")
                clickedPath(File(path.parent))
        }
        ContentTag(
            isLeft = false,
            color = color,
            text = parentFolder
        ){
            if (currentPath != "")
                clickedPath(File(path.parent))
        }
        if (currentPath != "")
            ContentTag(
                isLeft = false,
                color = color,
                text = currentPath
            ){

            }

    }
}

@Composable
private fun ContentTag(isLeft: Boolean, color: Color, text: String, fontSize: TextUnit = 14.sp, onClick: () -> Unit){
    Surface(
        shape = if (isLeft)
                    AbsoluteCutCornerShape(topLeftPercent = 50, bottomLeftPercent = 50)
                else
            AbsoluteCutCornerShape(topRightPercent = 50, bottomRightPercent = 50),
        modifier = Modifier.padding(8.dp).clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .background(color)
                .padding(
                    start = if(isLeft) fontSize.value.dp * 1.1f else fontSize.value.dp / 2,
                    end = if(isLeft) fontSize.value.dp / 2 else fontSize.value.dp * 1.1f,
                    top = 4.dp,
                    bottom = 4.dp,
                )
        ) {
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.h6,
                fontSize = fontSize,
                fontWeight = FontWeight.W300,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
    }
}