package momo.kikiplus.refactoring.view.recycler

import android.widget.Button
import lombok.AllArgsConstructor
import lombok.Data

@Data
@AllArgsConstructor(staticName = "of")
class WriteRecyclerItem {
    lateinit var text : String
    lateinit var modifyButton : Button
    lateinit var deleteButton : Button
}