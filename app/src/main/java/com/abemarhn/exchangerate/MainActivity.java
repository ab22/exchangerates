package com.abemarhn.exchangerate;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final String PURCHASE_KEY = "compra";
    private static final String SALE_KEY = "venta";
    private static final String ERR_KEY = "get_exchange_rates_error";

    private ProgressBar loadingSpinner;
    private FrameLayout loadingOverlay;
    private TextView txtPurchase;
    private TextView txtSale;
    private TextView txtLastUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtPurchase = findViewById(R.id.txtPurchase);
        txtSale = findViewById(R.id.txtSale);
        txtLastUpdated = findViewById(R.id.txtLastUpdated);
        loadingSpinner = findViewById(R.id.loadingSpinner);
        loadingOverlay = findViewById(R.id.loadingOverlay);

        loadExchangeRates();
    }

    private void loadExchangeRates() {
        loadingSpinner.setVisibility(View.VISIBLE);
        loadingOverlay.setVisibility(View.VISIBLE);
        new Thread(webScraper).start();
    }

    public void OnUpdateClick(View view) {
        loadExchangeRates();
    }

    @SuppressLint("HandlerLeak")
    private final Handler webScraperHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String error = bundle.getString(ERR_KEY);

            loadingSpinner.setVisibility(View.GONE);
            loadingOverlay.setVisibility(View.GONE);

            if (error != null) {
                Log.e("AppError", error);
                return;
            }

            SimpleDateFormat lastUpdated = new SimpleDateFormat("hh:mm:ss aa dd/MM/YYYY");
            txtLastUpdated.setText(lastUpdated.format(new Date()));
            String purchase = bundle.getString(PURCHASE_KEY);
            String sale = bundle.getString((SALE_KEY));

            if (!purchase.isEmpty()) {
                txtPurchase.setText("Lps." + purchase);
            }

            if (!sale.isEmpty()) {
                txtSale.setText("Lps." + sale);
            }
        }
    };

    private final Runnable webScraper = new Runnable() {
        @Override
        public void run() {
            Message msg = webScraperHandler.obtainMessage();
            Bundle bundle;
            try {
                bundle = scrapeExchangeRates();
            } catch (IOException ex) {
                bundle = new Bundle();
                bundle.putString(ERR_KEY, ex.getMessage());
                return;
            }

            msg.setData(bundle);
            webScraperHandler.sendMessage(msg);
        }

        private Bundle scrapeExchangeRates() throws IOException{
            Bundle bundle = new Bundle();

            Document html = Jsoup.connect("http://www.bancocci.hn/").get();
            Elements purchaseElems = html.select("span#usd-compra");
            Elements saleElems = html.select("span#usd-venta");

            if (purchaseElems.size() > 0) {
                bundle.putString(PURCHASE_KEY, purchaseElems.first().text());
            }

            if (saleElems.size() > 0) {
                bundle.putString(SALE_KEY, saleElems.first().text());
            }

            return bundle;
        }
    };
}
