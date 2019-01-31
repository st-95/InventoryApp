package com.example.thomasschmelzle.books;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.thomasschmelzle.books.data.BookContract.BookEntry;

// Allows user to create a new book or edit an existing one
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    // Identifies the book data loader
    private static final int EXISTING_Book_LOADER = 0;
    // regular price expression
    private final String regPriceExp = "[0-9]+([,.][0-9]{1,2})?";

    // Content URI for the existing book
    private Uri mCurrentBookUri;
    private EditText mProductNameEditText;
    private EditText mPriceEditText;
    private TextView mQuantityTextView;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneEditText;
    private Button mButtonIncrease;
    private Button mButtonDecrease;

    // Variable that keeps track of whether the book has been edited or not
    private boolean mBookHasChanged = false;

    // OnTouchListener that recognizes, if a user touches on a view
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.thomasschmelzle.books.R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if creating a new book or editing an existing one.
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // if intent does not contain a book URI, create a new book
        if (mCurrentBookUri == null) {
            // app bar says "Add a book"
            setTitle(getString(com.example.thomasschmelzle.books.R.string.editor_activity_title_new_book));

            // Delete option is not needed in this case
            invalidateOptionsMenu();
        } else {
            // if there is an existing book, app bar says "Edit book"
            setTitle(getString(com.example.thomasschmelzle.books.R.string.editor_activity_title_edit_book));

            // initialized loader to read book data from the database and display current values in the editor
            getLoaderManager().initLoader(EXISTING_Book_LOADER, null, this);
        }

        // Find all relevant views to read user input from
        mProductNameEditText = (EditText) findViewById(com.example.thomasschmelzle.books.R.id.edit_product_name);
        mSupplierNameEditText = (EditText) findViewById(com.example.thomasschmelzle.books.R.id.edit_supplier_name);
        mSupplierPhoneEditText = (EditText) findViewById(com.example.thomasschmelzle.books.R.id.edit_supplier_phone_number);
        mPriceEditText = (EditText) findViewById(com.example.thomasschmelzle.books.R.id.edit_price);
        mPriceEditText = (EditText) findViewById(com.example.thomasschmelzle.books.R.id.edit_price);
        mQuantityTextView = (TextView) findViewById(com.example.thomasschmelzle.books.R.id.quantity_text_view);
        mButtonIncrease = (Button) findViewById(com.example.thomasschmelzle.books.R.id.button_increase);
        mButtonDecrease = (Button) findViewById(com.example.thomasschmelzle.books.R.id.button_decrease);

        // increaseButton behavior
        mButtonIncrease.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer currentQuantity = Integer.parseInt(mQuantityTextView.getText().toString());
                currentQuantity ++;
                mQuantityTextView.setText(currentQuantity.toString());
            }
        });

        // decreaseButton behavior
        mButtonDecrease.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Integer currentQuantity = Integer.parseInt(mQuantityTextView.getText().toString());
                if(currentQuantity > 0) {
                    currentQuantity --;
                }
                mQuantityTextView.setText(currentQuantity.toString());
            }
        });

        // OnTouchListeners to all the input fields to know if there are unsaved changes and if the user tries to leave without saving
        mProductNameEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mButtonIncrease.setOnTouchListener(mTouchListener);
    }

    // get user input from editor and save book into database
    private void saveBook() {
        // Use trim to eliminate leading or trailing white space
        String productNameString = mProductNameEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = mSupplierPhoneEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString();
        String quantityString = mQuantityTextView.getText().toString();

        // Check if this is supposed to be a new book
        if (mCurrentBookUri == null &&
                TextUtils.isEmpty(productNameString) && TextUtils.isEmpty(supplierNameString) &&
                TextUtils.isEmpty(supplierPhoneNumberString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString)) {
            return;
        }
        if (TextUtils.isEmpty(productNameString)) {
            Toast.makeText(this, "Book requires a name. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(supplierNameString)) {
            Toast.makeText(this, "Book requires a supplier name. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(supplierPhoneNumberString)) {
            Toast.makeText(this, "Book requires a supplier phone number. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        else if (TextUtils.isEmpty(priceString) || !priceString.matches(regPriceExp)) {
            Toast.makeText(this, "Book requires a valid price. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            // create a ContentValues object with key - value pair
            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_PRODUCT_NAME, productNameString);
            values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
            values.put(BookEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneNumberString);
            values.put(BookEntry.COLUMN_PRICE, priceString);

            // integer value for quantity. Use 0 by default.
            int quantity = 0;
            if (!TextUtils.isEmpty(quantityString)) {
                quantity = Integer.parseInt(quantityString);
            }
            values.put(BookEntry.COLUMN_QUANTITY, quantity);

            // Determine if this is a new or existing pet by checking if mCurrentBookUri is null or not
            if (mCurrentBookUri == null) {
                // new book
                Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

                if (newUri == null) {
                    Toast.makeText(this, getString(com.example.thomasschmelzle.books.R.string.editor_insert_book_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(com.example.thomasschmelzle.books.R.string.editor_insert_book_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // book already exists. Update book with new values
                int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

                if (rowsAffected == 0) {
                    Toast.makeText(this, getString(com.example.thomasschmelzle.books.R.string.editor_update_book_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(com.example.thomasschmelzle.books.R.string.editor_update_book_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This adds menu items to the app bar.
        getMenuInflater().inflate(com.example.thomasschmelzle.books.R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(com.example.thomasschmelzle.books.R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case com.example.thomasschmelzle.books.R.id.action_save:
                saveBook();
                return true;
            case com.example.thomasschmelzle.books.R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_phone:
                phoneCall();
                return true;
            case android.R.id.home:
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // press back button
    @Override
    public void onBackPressed() {
        // If the book hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // projection that contains all columns from the book table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRICE,
                BookEntry.COLUMN_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int productNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_QUANTITY);

            String productName = cursor.getString(productNameColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);

            mProductNameEditText.setText(productName);
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(supplierPhone);
            mPriceEditText.setText(Double.toString(price));
            mQuantityTextView.setText(Integer.toString(quantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mProductNameEditText.setText("");
        mPriceEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");
        mQuantityTextView.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(com.example.thomasschmelzle.books.R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(com.example.thomasschmelzle.books.R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(com.example.thomasschmelzle.books.R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(com.example.thomasschmelzle.books.R.string.delete_dialog_msg);
        builder.setPositiveButton(com.example.thomasschmelzle.books.R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
            }
        });
        builder.setNegativeButton(com.example.thomasschmelzle.books.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void phoneCall() {
        if (mCurrentBookUri != null && mSupplierPhoneEditText != null) {
            String phoneNumber = mSupplierPhoneEditText.getText().toString().trim();
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null));
            startActivity(intent);
        }
    }

    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(com.example.thomasschmelzle.books.R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(com.example.thomasschmelzle.books.R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}