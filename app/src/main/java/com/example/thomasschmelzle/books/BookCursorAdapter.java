package com.example.thomasschmelzle.books;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.example.thomasschmelzle.books.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 );
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(com.example.thomasschmelzle.books.R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView productNameTextView = (TextView) view.findViewById(com.example.thomasschmelzle.books.R.id.product_name);
        TextView priceTextView = (TextView) view.findViewById(com.example.thomasschmelzle.books.R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(com.example.thomasschmelzle.books.R.id.quantity);

        int productNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);
        int idColumnIndex =cursor.getColumnIndex(BookEntry._ID);

        String productName = cursor.getString(productNameColumnIndex);
        String price = Double.toString(cursor.getDouble(priceColumnIndex));
        Integer quantity = cursor.getInt(quantityColumnIndex);
        final int id = cursor.getInt(idColumnIndex);

        productNameTextView.setText(productName);
        priceTextView.setText(price);
        quantityTextView.setText(quantity.toString());

        Button mButtonItemDecrease = view.findViewById(R.id.button_item_decrease);
        mButtonItemDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Integer currentQuantity = Integer.parseInt(quantityTextView.getText().toString());
                if (currentQuantity == 0){
                    Toast.makeText(context,"There are no books to sell", Toast.LENGTH_SHORT).show();
                }else{
                    // If there are still books to sell, reduce the quantity by 1 each time
                    currentQuantity = currentQuantity - 1;
                    quantityTextView.setText(Integer.toString(currentQuantity));
                    Uri currentBook = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_QUANTITY,quantityTextView.getText().toString());
                    //update the database
                    context.getContentResolver().update(currentBook,
                            values,
                            null,
                            null);
                }
            }
        });
    }
}
