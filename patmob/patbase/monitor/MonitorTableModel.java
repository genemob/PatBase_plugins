package patmob.patbase.monitor;

import javax.swing.table.AbstractTableModel;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Piotr
 */
public class MonitorTableModel extends AbstractTableModel {
    JSONArray tableRowArray;

    public MonitorTableModel(JSONObject tableData) {
        tableRowArray = tableData.getJSONArray("Families");
    }

    @Override
    public int getRowCount() {
        return tableRowArray.length();
    }

    @Override
    public int getColumnCount() {
        return tableRowArray.getJSONObject(0).length();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String fieldName;
        switch (columnIndex) {
            case 0:
                fieldName = "Pub Number";
                break;
            case 1:
                fieldName = "Family";
                break;
            default:
                fieldName = "Keywords";
        }
        return tableRowArray.getJSONObject(rowIndex).get(fieldName);
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Pub Number";
            case 1:
                return "Family";
            default:
                return "Keywords";
        }
    }
}
