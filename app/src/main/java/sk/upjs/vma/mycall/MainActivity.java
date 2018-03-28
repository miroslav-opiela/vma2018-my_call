package sk.upjs.vma.mycall;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleCursorAdapter;

/**
 * Aktivita implementuje LoaderCallback.
 */
public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {

    /**
     * Defaultny null-ovy kurzor.
     */
    private static final Cursor NO_CURSOR = null;
    /**
     * Defaultne ziadne flags.
     */
    private static final int NO_FLAGS = 0;

    /**
     * Vlastne ID loadera.
     */
    private static final int LOADER_ID = 1;
    /**
     * Defaultny nullovy bundle.
     */
    private static final Bundle NO_BUNDLE = null;

    /**
     * Request code na identifikaciu permission.
     */
    private static final int REQUEST_CODE = 8;

    /**
     * SimpleCursorAdapter ktory sprostredkuva data z cursora do daneho view.
     */
    private SimpleCursorAdapter adapter;
    /**
     * GridView - mriezka s telefonnymi cislami.
     */
    private GridView gridView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // referencia na grid view - musi byt na zaciatku metody, lebo ju vyuziva snackbar
        gridView = findViewById(R.id.gridView);

        // overenie ci ma aplikacia povolenie citat call log
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            // v pripade ze aplikacia ma povolenie, pokracuje sa dalej v metode init
            init();
        } else {
            // ak nema povolenie, overi sa ci ho ma vyziadat s vysvetlenim
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CALL_LOG)) {
                // vyziadanie s vysvetlenim, situacia nastava napr. ak bolo predtym zamietnute udelenie povolenia
                // vytvori sa snackbar, zmizne az po kliknuti ked sa vyziada povolenie
                Snackbar.make(gridView, "Potrebujes povolenie.", Snackbar.LENGTH_INDEFINITE)
                        .setAction("ok", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // ziada sa povolenie READ_CALL_LOG
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.READ_CALL_LOG},
                                        REQUEST_CODE);
                            }
                        }).show();

            } else {
                // vyziadanie povolenia bez detailneho vysvetlenia, toto nastava pri prvom spusteni
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CALL_LOG},
                        REQUEST_CODE);
            }
        }
    }

    /**
     * Medota sa vola po udeleni akehokolvek povolenia.
     *
     * @param requestCode  kod ziadosti o povolenie. Definuje sa v requestPermission metode.
     * @param permissions  pole povoleni, ktore boli udelene.
     * @param grantResults pole vysledkov k povoleniam.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // pokracuje sa v init metode ak ide o nami ziadane povolenie a bolo udelene
                init();
            }
        }
    }

    /**
     * Inicializacia aktivity. Vola sa po udeleni povolenia na READ_CALL_LOG.
     */
    private void init() {
        // pouzije sa loader manager na ziskanie loadera, ktory asynchronne nacita data
        // LOADER_ID musi byt unikatne vzhladom k danemu managerovi
        // callback implementuje tato aktivita
        getLoaderManager().initLoader(LOADER_ID, NO_BUNDLE, this);

        // stlpec odkial sa mapuju data
        String[] from = {CallLog.Calls.NUMBER};
        // View, kam sa mapuju data - v tomto pripade jedna polozka z gridView.
        // android.R.id.text1 je ID TextView v layout xml subore pre simple_list_item_1
        int[] to = {android.R.id.text1};

        // adapter, zatial s defaultnym nullovym kurzorom
        adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, NO_CURSOR, from, to, NO_FLAGS);

        adapter.setViewBinder(new CallLogViewBinder());

        // nastavenie daneho adaptera pre gridView
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(this);
    }


    /**
     * Callback metoda pri inicializacii loadera.
     *
     * @param id     id loadera unikatne vzhladom na loader managera.
     * @param bundle dodatocne budle data.
     * @return novy Loader.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        // identifikacia konkretneho vytvaraneho loadera
        if (id == LOADER_ID) {
            // vytvara sa cursorLoader pre data v 'tabulke' CallLog.Calls
            CursorLoader cursorLoader = new CursorLoader(this);
            cursorLoader.setUri(CallLog.Calls.CONTENT_URI);
            return cursorLoader;
        }
        return null;
    }

    /**
     * Callback metoda po nacitani dat loaderom.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // adapter si nastavi nacitany kurzor
        adapter.swapCursor(cursor);
    }

    /**
     * Callback metoda ak je vytvoreny loader zresetovany.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // adapter si nastavi defaultny nullovy kurzor
        adapter.swapCursor(NO_CURSOR);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        String text = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));
        Snackbar.make(gridView, text, Snackbar.LENGTH_LONG).show();
    }
}
