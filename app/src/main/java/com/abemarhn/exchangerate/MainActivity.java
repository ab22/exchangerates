package com.abemarhn.exchangerate;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String PURCHASE_KEY = "compra";
    private static final String SALE_KEY = "venta";
    private static final String ERR_KEY = "get_exchange_rates_error";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(runnable).start();
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            String error = bundle.getString(ERR_KEY);

            if (error != null) {
                Log.e("AppError", error);
                return;
            }

            String purchase = bundle.getString(PURCHASE_KEY);
            String sale = bundle.getString((SALE_KEY));

            TextView txtPurchase = findViewById(R.id.txtPurchase);
            TextView txtSale = findViewById(R.id.txtSale);

            txtPurchase.setText("Lps." + purchase);
            txtSale.setText("Lps." + sale);
        }
    };

    private final Runnable runnable = new Runnable() {
        @Override
        public void run() {
            Message msg = mHandler.obtainMessage();
            Bundle bundle;
            try {
                bundle = getExchangeRates();
            } catch (IOException ex) {
                bundle = new Bundle();
                bundle.putString(ERR_KEY, ex.getMessage());
                return;
            }

            msg.setData(bundle);
            mHandler.sendMessage(msg);
        }

        private Bundle getExchangeRates() throws IOException{
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
