package ir.appservice_v1.view.appComponent;

import lombok.Getter;
import lombok.Setter;
import org.primefaces.component.api.UIColumn;
import org.primefaces.component.celleditor.CellEditor;

import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.io.IOException;
import java.util.List;

@Setter
@Getter
public class AppUIColumn<T> implements UIColumn {

    protected Class<T> itemClass;
    protected MethodExpression filterFunction;
    protected MethodExpression exportFunction;
    protected MethodExpression sortFunction;
    protected ValueExpression valueExpression;
    protected List<UIComponent> children;
    protected CellEditor cellEditor;
    protected UIComponent facet;
    protected Object filterBy;
    protected Object filterValue;
    protected Object filterOptions;
    protected Object sortBy;

    public AppUIColumn(Class<T> itemClass) {
        this.itemClass = itemClass;
    }

    @Override
    public ValueExpression getValueExpression(String property) {
        return valueExpression;
    }

    @Override
    public String getContainerClientId(FacesContext context) {
        return null;
    }

    @Override
    public String getColumnKey() {
        return null;
    }

    @Override
    public String getClientId() {
        return null;
    }

    @Override
    public String getClientId(FacesContext context) {
        return null;
    }

    @Override
    public String getSelectionMode() {
        return null;
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    @Override
    public String getStyle() {
        return null;
    }

    @Override
    public String getStyleClass() {
        return null;
    }

    @Override
    public int getRowspan() {
        return 0;
    }

    @Override
    public int getColspan() {
        return 0;
    }

    @Override
    public String getFilterPosition() {
        return null;
    }

    @Override
    public UIComponent getFacet(String facet) {
        return null;
    }

    @Override
    public Object getFilterBy() {
        return filterBy;
    }

    @Override
    public Object getFilterValue() {
        return filterValue;
    }

    @Override
    public String getHeaderText() {
        return null;
    }

    @Override
    public String getFooterText() {
        return null;
    }

    @Override
    public String getFilterStyleClass() {
        return null;
    }

    @Override
    public String getFilterStyle() {
        return null;
    }

    @Override
    public String getFilterMatchMode() {
        return null;
    }

    @Override
    public int getFilterMaxLength() {
        return 0;
    }

    @Override
    public Object getFilterOptions() {
        return filterOptions;
    }

    @Override
    public CellEditor getCellEditor() {
        return cellEditor;
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public MethodExpression getSortFunction() {
        return sortFunction;
    }

    @Override
    public Object getSortBy() {
        return sortBy;
    }

    @Override
    public List<UIComponent> getChildren() {
        return children;
    }

    @Override
    public boolean isExportable() {
        return false;
    }

    @Override
    public boolean isRendered() {
        return false;
    }

    @Override
    public void encodeAll(FacesContext context) throws IOException {

    }

    @Override
    public void renderChildren(FacesContext context) throws IOException {

    }

    @Override
    public String getWidth() {
        return null;
    }

    @Override
    public boolean isToggleable() {
        return false;
    }

    @Override
    public MethodExpression getFilterFunction() {
        return filterFunction;
    }

    @Override
    public String getField() {
        return null;
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isSortable() {
        return false;
    }

    @Override
    public boolean isFilterable() {
        return false;
    }

    @Override
    public boolean isVisible() {
        return false;
    }

    @Override
    public boolean isSelectRow() {
        return false;
    }

    @Override
    public String getAriaHeaderText() {
        return null;
    }

    @Override
    public MethodExpression getExportFunction() {
        return exportFunction;
    }

    @Override
    public boolean isGroupRow() {
        return false;
    }
}
