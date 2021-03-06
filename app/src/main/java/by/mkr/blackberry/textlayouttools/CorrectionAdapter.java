package by.mkr.blackberry.textlayouttools;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sateda.keyonekb.R;

import java.util.List;


class CorrectionViewItem {
    public long id;
    public String fromText;
    public String toText;

    public CorrectionViewItem() {
        this(0, "", "");
    }

    public CorrectionViewItem(long id, String from, String to) {
        this.id = id;
        this.fromText = from;
        this.toText = to;
    }
}

class CorrectionItem {
    public static final String FROM_TO_DELIMITER = "»";
    public static final String ITEMS_DELIMITER = "¦";
    public static final String DEFAULT_VALUE =
            "ожон"+FROM_TO_DELIMITER+"оддон"+ITEMS_DELIMITER
            +"ожан"+FROM_TO_DELIMITER+"оддан"+ITEMS_DELIMITER
            +"ожерж"+FROM_TO_DELIMITER+"оддерж"+ITEMS_DELIMITER
            +"ожуб"+FROM_TO_DELIMITER+"оддуб"+ITEMS_DELIMITER
            +"уюот"+FROM_TO_DELIMITER+"уббот"+ITEMS_DELIMITER
            +"стрэ"+FROM_TO_DELIMITER+"стрее";

    public String from;
    public String to;

    public CorrectionItem(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public CorrectionItem(String stringValue) {
        String[] values = stringValue.split(CorrectionItem.FROM_TO_DELIMITER);
        this.from = values[0];
        this.to = values[1];
    }

    @Override
    public String toString() {
        return this.from + FROM_TO_DELIMITER + this.to;
    }
}

public class CorrectionAdapter extends RecyclerView.Adapter<CorrectionAdapter.MyViewHolder> {


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView itemFromText;
        public TextView itemToText;
        public ImageButton btnItemDelete;
        public int itemIndex;

        public LinearLayout layoutView;


        public MyViewHolder(final LinearLayout layoutView, final CorrectionsListener listener) {
            super(layoutView);
            itemFromText = (TextView) layoutView.findViewById(R.id.textViewItemFrom);
            itemToText = (TextView) layoutView.findViewById(R.id.textViewItemTo);
            btnItemDelete = (ImageButton) layoutView.findViewById(R.id.btnItemDelete);
            this.layoutView = layoutView;

            // Item Click opens popup edit dialog
            layoutView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CorrectionAdapter.showItemEditDialog(
                            layoutView.getContext(),
                            itemFromText.getText().toString(),
                            itemToText.getText().toString(),
                            new ItemEditDialogListener() {
                                @Override
                                public void onOk(String newFromText, String newToText) {
                                    if (listener != null) {
                                        listener.onItemEdited(itemIndex, newFromText, newToText);
                                    }
                                }
                            });
                }
            });



            // Button click deletes item
            btnItemDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(layoutView.getContext());
                    builder
                            .setCancelable(true)
                            .setTitle(builder.getContext().getString(R.string.dialog_delete_title))
                            .setMessage(itemFromText.getText() + " => " + itemToText.getText())
                            .setPositiveButton(builder.getContext().getString(R.string.dialog_yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    listener.onItemDeleted(itemIndex);
                                }
                            })
                            .setNegativeButton(builder.getContext().getString(R.string.dialog_no), null);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }

        public void bind(CorrectionViewItem value) {
            Log.d("ReplacerLog", "bind MyViewHolder");
            itemFromText.setText(value.fromText);
            itemToText.setText(value.toText);
        }

    }



    private final List<CorrectionViewItem> _values;
    private CorrectionsListener _listener;

    public CorrectionAdapter(List<CorrectionViewItem> values) {
        _values = values;
        Log.d("ReplacerLog", "values set; " + values.size());
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LinearLayout layoutView = (LinearLayout) LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.tlt_content_correction_item, viewGroup, false);
        MyViewHolder vh = new MyViewHolder(layoutView, _listener);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, @SuppressLint("RecyclerView") int i) {
        myViewHolder.itemFromText.setText(_values.get(i).fromText);
        myViewHolder.itemToText.setText(_values.get(i).toText);
        myViewHolder.itemIndex = i;


        // Open dialog for created correction
        /*
        Log.d("ReplacerLog", "text=" + myViewHolder.packageName.getText().toString());
        if (myViewHolder.packageName.getText().toString().equals("")) {
            Log.d("ReplacerLog", "call click");
            myViewHolder.layoutView.callOnClick();
        }
        */
    }

    @Override
    public int getItemCount() {
        return _values.size();
    }

    public void addItem(Context context) {
        CorrectionAdapter.showItemEditDialog(context, "", "", new ItemEditDialogListener() {
            @Override
            public void onOk(String newFromText, String newToText) {
                if (_listener != null) {
                    _listener.onItemAdded(0, newFromText, newToText);
                }
            }
        });

        /*if (_listener != null) {
            _listener.onItemAdded(-1);
        }*/
    }

    public void setListener(final CorrectionsListener listener) {
        this._listener = listener;
    }

    public static void showItemEditDialog(Context context, String fromText, String toText, final ItemEditDialogListener listener) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View promptsView = inflater.inflate(R.layout.tlt_content_correction_prompt, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(promptsView);

        final EditText inputFrom = (EditText) promptsView.findViewById(R.id.editText);
        final EditText inputTo = (EditText) promptsView.findViewById(R.id.editText2);
        inputFrom.setText(fromText);
        inputTo.setText(toText);

        builder
                .setCancelable(true)
                .setPositiveButton(R.string.dialog_ok, null) // Add action with validation later
                .setNegativeButton(R.string.dialog_cancel, null);
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // Set positive button action with validation
        Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (inputFrom.getText().toString().equals("")) {
                            inputFrom.setError(alertDialog.getContext().getString(R.string.dialog_error_empty_input));
                        } else {
                            listener.onOk(inputFrom.getText().toString(), inputTo.getText().toString());
                            alertDialog.dismiss();
                        }
                    }
                });
    }

    public interface ItemEditDialogListener {
        void onOk(String newFromText, String newToText);
    }

    public interface CorrectionsListener {
        void onItemAdded(int itemIndex, String newFromText, String newToText);
        void onItemEdited(int itemIndex, String newFromText, String newToText);
        void onItemDeleted(int itemIndex);
    }

}
