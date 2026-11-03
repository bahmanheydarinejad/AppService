package ir.appservice_v1.view.appComponent;

import org.primefaces.component.api.UIColumn;
import org.primefaces.component.commandbutton.CommandButton;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.link.Link;

import java.util.ArrayList;
import java.util.List;

public class AppDataTable<T> extends DataTable {

    protected Class<T> itemClass;

    public AppDataTable(Class<T> itemClass) {
        this.itemClass = itemClass;
    }

    private void initDataTable() {
        setId(itemClass.getSimpleName() + "_TABLE");
        setWidgetVar(itemClass.getSimpleName() + "_TABLE");
    }

    public void initDataTableColumns() {
        List<UIColumn> columns = new ArrayList<>();
        setColumns(columns);

        CommandButton cm = new CommandButton();
        Link link = new Link();
    }
}
