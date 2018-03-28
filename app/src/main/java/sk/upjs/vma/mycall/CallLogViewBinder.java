package sk.upjs.vma.mycall;

import android.database.Cursor;
import android.graphics.Color;
import android.provider.CallLog;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class CallLogViewBinder implements SimpleCursorAdapter.ViewBinder {
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (!(view instanceof TextView)) {
            return false;
        }

        TextView textView = (TextView) view;
        int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
        switch (type) {
            case CallLog.Calls.INCOMING_TYPE:
                textView.setBackgroundColor(Color.BLUE);
                break;
            case CallLog.Calls.OUTGOING_TYPE:
                textView.setBackgroundColor(Color.GREEN);
                break;
            case CallLog.Calls.MISSED_TYPE:
                textView.setBackgroundColor(Color.RED);
                break;
        }

        return false;
    }
}
