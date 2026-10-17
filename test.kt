import androidx.compose.material3.*
import androidx.compose.runtime.Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Test() {
    ExposedDropdownMenuBox(expanded=false, onExpandedChange={}) {
        ExposedDropdownMenu(expanded=false, onDismissRequest={}) {
            DropdownMenuItem(text={}, onClick={})
        }
    }
}
