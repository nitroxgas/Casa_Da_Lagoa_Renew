package br.com.casadalagoa.casadalagoa.casadalagoatabs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.http.SslError;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.Format;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class Principal extends ActionBarActivity implements ActionBar.TabListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    ViewPager mViewPager;

    Timer timer;
    MyTimerTask myTimerTask;
    boolean showConfig = false;
    boolean showGrafico = false;
    boolean conectar = true;

    // Definições para agendamentos
    boolean verAgenda = false;
    public int procurandoServidor = 0;
    private Cursor mCursor = null;
    private static final String[] COLS = new String[]
            {CalendarContract.Events.TITLE, CalendarContract.Events.DTSTART, CalendarContract.Events.CALENDAR_DISPLAY_NAME};



    //final String[] estado = {"0","0","0","0","0","0","0","0","0","0","0"};
    public String servidor = "https://docs.google.com/spreadsheet/pub?key=0AthpB0DCO-YadE5tcC1BVWRzSnNBRkRmLTJfaGhTOFE&single=true&gid=0&range=A1&output=csv";
    public String servidorGrafico = "https://docs.google.com/spreadsheet/oimg?key=0AthpB0DCO-YadE5tcC1BVWRzSnNBRkRmLTJfaGhTOFE&oid=1&zx=uz4jqb2kuxjw";
    public String strPlanilha ="https://docs.google.com/spreadsheet/pub?key=0AthpB0DCO-YadE5tcC1BVWRzSnNBRkRmLTJfaGhTOFE&single=true&gid=0&range=A1&output=csv";
    public SharedPreferences mPrefs;
    public String LOG_TAG = "FILTRAR";

    public  HttpAsyncTask mHttpAsyncTask = new HttpAsyncTask();

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    protected void onResume() {
        super.onResume();

        showConfig = mPrefs.getBoolean("showConfig", false);
        showGrafico = mPrefs.getBoolean("showGrafico", false);

        String atualizar = mPrefs.getString("sync_frequency", "-1");
        if (!atualizar.equals("-1")) {
            timer = new Timer();
            myTimerTask = new MyTimerTask();
            timer.schedule(myTimerTask, 30000 * Integer.valueOf(atualizar));
            if (showConfig)
                Toast.makeText(getBaseContext(), "(Iniciou Timer " + atualizar + " minutos)", Toast.LENGTH_SHORT).show();
        }
    }

/*
        WebView tela = (WebView) findViewById(1001);
        if ((tela!=null)){
            if (showGrafico) {
                tela.setVisibility(View.VISIBLE);
                tela.loadUrl(servidorGrafico);
            }
            else
              tela.setVisibility(View.INVISIBLE);
        }
        if (showConfig) Log.d(LOG_TAG,"onResume");
*/


    @Override
    protected void onPause() {
        if (timer != null ){
            timer.cancel();
            if (showConfig)
                Toast.makeText(getBaseContext(), "(Parou Timer)", Toast.LENGTH_SHORT).show();
        }
        if (showConfig) Log.v(LOG_TAG,"onPause");
        super.onPause();
    }

    private void Conectar() {
        // Verifica se há conexão e faz solicitação inicial
        if (isConnected()) {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
           // NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (activeNetInfo != null) {
                if (activeNetInfo.getTypeName().toString().contains("WIFI")) {
                    if (showConfig) Toast.makeText(getBaseContext(), "WIFI SSID (" + getCurrentSsid(this) + ")", Toast.LENGTH_LONG).show();
                    if (getCurrentSsid(this).contains("GeorgeHome")) {
                        servidor = mPrefs.getString("servidor_casa", "http://192.168.1.220/");
                        new HttpAsyncTask().execute(servidor);
                        if (showConfig) Toast.makeText(getBaseContext(), "Acessando rede local...", Toast.LENGTH_LONG).show();
                        conectar=false;
                    }
                } else {
                    servidor = mPrefs.getString("servidor", strPlanilha);
                    Toast.makeText(this, "Rede Móvel", Toast.LENGTH_SHORT).show();
                    new HttpAsyncTask().execute(servidor);
                    conectar=false;
                }
            }
        } else {
            Toast.makeText(getBaseContext(), "Não Conectado!", Toast.LENGTH_LONG).show();
        }
        //if (showConfig) Log.v(LOG_TAG,"Conectar");
    }

    public boolean isEventInCal(Context context, String cal_meeting_id) {

        Cursor cursor = context.getContentResolver().query(
                Uri.parse("content://com.android.calendar/events"),
                new String[] { "_id" }, " _id = ? ",
                new String[] { cal_meeting_id }, null);

        if (cursor.moveToFirst()) {
            //will give all events
            return true;
        }
        return false;
    }

    public void clickMenos(View view){
        new HttpAsyncTask().execute(servidor+"?relay=TVSALA&op=VMenos");
    }

    public void ajustaBotoes( String[] estado) {

        Button portao = (Button) findViewById(100);
        implementaBotao(portao, "?relay=50.", "Abrir o portão ?",R.drawable.ic_remote, estado, true);

        Button alimentador = (Button) findViewById(101);
        implementaBotao(alimentador, "?relay=RACAO",  "Acionar o alimentador ?",R.drawable.ic_dog_g, estado, true);

        Button internet = (Button) findViewById(102);
        implementaBotao(internet, "?relay=INTERNET", "Reiniciar a internet ?",R.drawable.ic_remote, estado, true);

        Button tv = (Button) findViewById(R.id.bt_tvsala);
        implementaBotao(tv, "TV_SALA", "Ligar/Desligar TV ?",R.drawable.ic_remote, estado, false);

        Button lareira = (Button) findViewById(R.id.bt_lareira);
        implementaBotao(lareira, "LR_SALA", "Ligar/Desligar Lareira ?",R.drawable.ic_remote, estado, false);

        Button lig_todas = (Button) findViewById(R.id.bt_ligatudo);
        implementaBotao(lig_todas, "acende externa", "Ligar Luzes Externas ?",R.drawable.ic_remote, estado, true);

        Button lig_internas = (Button) findViewById(R.id.bt_liga_internas);
        implementaBotao(lig_internas, "acende interna", "Ligar Luzes Internas ?",R.drawable.ic_remote, estado, true);

        Button tv_liga = (Button) findViewById(R.id.tv_liga);
        implementaBotao(tv_liga, "?relay=TVSALA&op=PWR", "--",R.drawable.ic_remote, estado, false);

        Button tv_v_mais = (Button) findViewById(R.id.tv_vol_mais);
        implementaBotao(tv_v_mais, "?relay=TVSALA&op=VMais", "--",R.drawable.ic_remote, estado, false);
                    /*
                    Button tv_v_menos = (Button) findViewById(R.id.tv_vol_menos);
                    implementaBotao(tv_v_menos, "?relay=TVSALA&op=VMenos", "--",R.drawable.ic_remote, estado, false);
*/
        Button tv_prox = (Button) findViewById(R.id.tv_left);
        implementaBotao(tv_prox, "?relay=TVSALA&op=Prox", "--",R.drawable.ic_remote, estado, false);

        Button tv_source = (Button) findViewById(R.id.tv_input);
        implementaBotao(tv_source, "?relay=TVSALA&op=Input", "--",R.drawable.ic_remote, estado, false);

        Button tv_ok = (Button) findViewById(R.id.tv_ok);
        implementaBotao(tv_ok, "?relay=TVSALA&op=OK", "--",R.drawable.ic_remote, estado, false);

        Button tv_ca_mais = (Button) findViewById(R.id.tv_ca_mais);
        implementaBotao(tv_ca_mais, "?relay=TVSALA&op=CMais", "--",R.drawable.ic_remote, estado, false);

        Button tv_ca_menos = (Button) findViewById(R.id.tv_ca_menos);
        implementaBotao(tv_ca_menos, "?relay=TVSALA&op=CMenos", "--",R.drawable.ic_remote, estado, false);

        Button tv_ant = (Button) findViewById(R.id.tv_ant);
        implementaBotao(tv_ant, "?relay=TVSALA&op=Ant", "--",R.drawable.ic_remote, estado, false);

        // Botoes da lareira

        Button lr_liga = (Button) findViewById(R.id.bt_lr_liga);
        implementaBotao(lr_liga, "?relay=LRSALA&op=PWR", "--",R.drawable.ic_remote, estado, false);

        Button lr_chama = (Button) findViewById(R.id.bt_lr_ch);
        implementaBotao(lr_chama, "?relay=LRSALA&op=CHM", "--",R.drawable.ic_remote, estado, false);

        Button lr_med = (Button) findViewById(R.id.bt_lr_med);
        implementaBotao(lr_med, "?relay=LRSALA&op=MED", "--",R.drawable.ic_remote, estado, false);

        Button lr_high = (Button) findViewById(R.id.bt_lr_alta);
        implementaBotao(lr_high, "?relay=LRSALA&op=HIGH", "--",R.drawable.ic_remote, estado, false);
    }

    @Override
    protected void onStop() {
        if (timer!=null) timer.cancel();
        if (showConfig) Log.v(LOG_TAG, "onStop");
        super.onStop();
    }

    public String getLocalIpAddress()
    {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Net", ex.toString());
        }

        return null;
    }

    public String getCurrentSsid(Context context) {

        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !(connectionInfo.getSSID().equals(""))) {
                //if (connectionInfo != null && !StringUtil.isBlank(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }
        return ssid;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_principal);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        showConfig = mPrefs.getBoolean("showConfig",false);
        showGrafico = mPrefs.getBoolean("showGrafico",false);

        // getSharedPreferences("default", 0);
        //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Tratamento de calendário

        if (isEventInCal(this,"Lareira") == true) {
            Toast.makeText( this, "Tem Evento", Toast.LENGTH_SHORT ).show();
        }
        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setSubtitle("Acione os automatismos da casa...");
        actionBar.setIcon(R.drawable.bg_lagoa);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        //actionBar.setSelectedNavigationItem(2); if (conectar)
       // Log.v(LOG_TAG,"onCreate");
        Conectar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        ActionBar actionBar = getSupportActionBar();
         // WebView tela = (WebView)  this.mViewPager.findViewById(1001);
        switch(item.getItemId()) {
            case R.id.menu_toggle_log:
                // Abre configurações
                startActivity(new Intent(this, SettingsActivity.class));
                break;
               // showConfig=!showConfig;
                /*
                SharedPreferences.Editor mEditor = mPrefs.edit();
                mEditor.putBoolean("trace", showConfig).commit();
                actionBar.setSelectedNavigationItem(2);
                if ((tela!=null)&&(tela.getVisibility()==View.INVISIBLE)){
                    tela.setVisibility(View.VISIBLE);
                    tela.loadUrl("https://www.statuscake.com/App/button/index.php?Track=CRFiUIELKv&Days=1&Design=1");
                } else tela.setVisibility(View.INVISIBLE);

            case R.id.menu_agenda:
                verAgenda=!verAgenda;
                break;

            case R.id.menu_grafico:
                actionBar.setSelectedNavigationItem(2);
                if ((tela!=null)&&(tela.getVisibility()==View.INVISIBLE)){
                    tela.setVisibility(View.VISIBLE);
                    tela.loadUrl(servidorGrafico);
                } else tela.setVisibility(View.INVISIBLE);
                break;
            case R.id.menu_atualizar:
                //new HttpAsyncTask().execute(strPlanilha);
                break;
                */
            case R.id.menu_devices: actionBar.setSelectedNavigationItem(0);  break;
            case R.id.menu_external: actionBar.setSelectedNavigationItem(1);  break;
            case R.id.menu_temp: actionBar.setSelectedNavigationItem(2);  break;
            case R.id.menu_sair: this.finish(); return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        //   if (!servidor.contains("https://docs")) new HttpAsyncTask().execute(servidor);
        mViewPager.setCurrentItem(tab.getPosition());
    }

     /*
         Timer para atualizar o conteúdo efetuando nova consulta.
     */
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    HttpAsyncTask mHttpAsyncTask = (HttpAsyncTask) new HttpAsyncTask().execute(servidor);
                    if (showConfig) Toast.makeText(getBaseContext(), "(Atualizando)", Toast.LENGTH_SHORT).show();
                    if (showGrafico) {
                        WebView tela = (WebView) findViewById(1001);
                        if (tela!=null) tela.loadUrl(servidorGrafico);
                    }
                }});

        }
    }


    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 4 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 2:
                    return getString(R.string.title_section4).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 0:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view. static
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        // static
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        public void criaChave(View rootView, String[] conteudo, int sec){
            LinearLayout chaves = (LinearLayout) rootView.findViewById(R.id.lay_chaves);
            for (int i=0;i<conteudo.length ;i++) {
                Switch chave_nova = new Switch(this.getActivity());
                chave_nova.setText(conteudo[i]);
                chave_nova.setLayoutParams(rootView.findViewById(R.id.switch1).getLayoutParams());
                chave_nova.setId(i + (100 * sec));
                chave_nova.setTextOff("Desligado");
                chave_nova.setTextOn("Ligado");
                chave_nova.setBackgroundColor(getResources().getColor(R.color.black_overlay));
                chave_nova.setTextAppearance(chave_nova.getContext(),R.style.ChaveTextAppearance);
                chave_nova.setSwitchTextAppearance(chave_nova.getContext(),R.style.SwitchTextAppearance);
                if (conteudo[i].equals("Reservado")) {
                    chave_nova.setActivated(false);
                    chave_nova.setEnabled(false);
                }
                chaves.addView(chave_nova);
            }
        }

        // Declara chaves já disponíveis
        public final String[] arr_Interior = { "Quarto", "Closet", "Cabeceira G", "Cabeceira C", "Escritório", "Escada", "Balcão", "Mesas", "Superior"  };
        public final String[] arr_Exterior = { "Fundos", "Área de Serviço" };

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            View botaoView = rootView.findViewById(R.id.lay_botao);
            View chaveView = rootView.findViewById(R.id.lay_chaves);
            View tempView = rootView.findViewById(R.id.lay_temp);

            /*
              Ajusta as telas...
            */

            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 3:// Temperatura
                    botaoView.setVisibility(View.GONE);
                    chaveView.setVisibility(View.GONE);
                    tempView.setVisibility(View.VISIBLE);
                    TextView tempPlaca = (TextView) tempView.findViewById(R.id.tmpPlaca);
                    tempPlaca.setId(401);
                    TextView tempExt = (TextView) tempView.findViewById(R.id.tmpExt);
                    tempExt.setId(402);
                    final TextView tempInt = (TextView) tempView.findViewById(R.id.tmpInt);
                    tempInt.setId(403);

                    WebView tela = (WebView) tempView.findViewById(R.id.webGraficoTemperaturas);
                    tela.setId(1001);
                    tela.getSettings().setJavaScriptEnabled(false);
                    tela.setEnabled(true);
                    //tela.setInitialScale(250);
                    tela.getSettings().setBuiltInZoomControls(true);

                    tela.setWebChromeClient(new WebChromeClient() {

                        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                            tempInt.setText(String.format("Oh no! %s", description));
                        }

                        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError
                                error) {
                            // Ignora erros de SSL, nosso certificado é autoassinado
                            handler.proceed();
                        }
                    });
                    tela.clearSslPreferences();
                    tela.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                    SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
                    //boolean showConfig  = mPrefs.getBoolean("showConfig",false);
                    boolean showGrafico = mPrefs.getBoolean("showGrafico",false);
                    if (showGrafico) {
                        tela.setVisibility(View.VISIBLE);
                        tela.loadUrl("https://docs.google.com/spreadsheets/d/e/2PACX-1vTKRkuEEvqvu9J_q4558nLrlBHakz7rhwYbFifu38doQ3FnDBNLvJndlO8oIPEcJA2oX8sfKcz5929n/pubchart?oid=1&format=image");
                                    //https://docs.google.com/spreadsheets/d/e/2PACX-1vTKRkuEEvqvu9J_q4558nLrlBHakz7rhwYbFifu38doQ3FnDBNLvJndlO8oIPEcJA2oX8sfKcz5929n/pubchart?oid=1&amp;format=image");
                                //"https://docs.google.com/spreadsheet/oimg?key=0AthpB0DCO-YadE5tcC1BVWRzSnNBRkRmLTJfaGhTOFE&oid=1&zx=uz4jqb2kuxjw");
                    } else tela.setVisibility(View.INVISIBLE);
                    // Não funcionou, precisou fixar o ID em 1001
                    //tela.setVisibility(View.INVISIBLE);
                    //String fonteGrafico = "<img src=\"https://docs.google.com/spreadsheet/oimg?key=0AthpB0DCO-YadE5tcC1BVWRzSnNBRkRmLTJfaGhTOFE&oid=1&zx=uz4jqb2kuxjw\" />";
                    //tela.loadDataWithBaseURL("https://docs.google.com/spreadsheet/",fonteGrafico,"text/html",null,null);
                    break; //  textView.setTag(getArguments().getInt(ARG_SECTION_NUMBER), tempObj);

                case 2: // Exterior
                    botaoView.setVisibility(View.GONE);
                    chaveView.setVisibility(View.VISIBLE);
                    tempView.setVisibility(View.GONE);
                    criaChave(rootView, arr_Exterior,2);
                    criaChave(rootView, arr_Interior,3);
                    break;

                case 1: //Dispositivos
                    chaveView.setVisibility(View.GONE);
                    tempView.setVisibility(View.GONE);
                    botaoView.setVisibility(View.VISIBLE);
                    Button portao = (Button) botaoView.findViewById(R.id.bt_portao);
                    portao.setId(100);
                    Button alim = (Button) botaoView.findViewById(R.id.bt_alim);
                    alim.setId(101);
                    Button internet = (Button) botaoView.findViewById(R.id.bt_inter);
                    internet.setId(102);
                    //botaoView.findViewById(R.id.button).setOnClickListener();
            }
                rootView.findViewById(R.id.switch1).setVisibility(View.GONE);
     //           Log.v("FILTRAR","onCreateView Fragment");
            return rootView;
        }


    }

    //
    // Sessão para tratar das comunicações:
    //
    public static String GET(String url){
        InputStream inputStream ;
        String result;
        try {

            // create HttpClient
            DefaultHttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            }
            else
                result = "Não funcionou";


        } catch (Exception e) {
            result = "Exception no recebimento..." + e.toString();
           // this.servidor = "http://186.222.50.176:8080";
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())

            return true;
        else
            return false;
    }

    public void ajustacor( TextView textView, String retorno){
        try {
            int temperatura = 0;

           /*
            temperatura = Long.parseLong(retorno);
            */

            temperatura = Integer.parseInt(retorno); //.substring(0,retorno.indexOf(".")

            if (temperatura < 20)
                textView.setTextColor(textView.getResources().getColor(R.color.c_azul));
            else if ((temperatura >= 20) && (temperatura < 30))
                textView.setTextColor(textView.getResources().getColor(R.color.c_verde));
            else if ((temperatura >= 30) && (temperatura < 40))
                textView.setTextColor(textView.getResources().getColor(R.color.c_laranja));
            else if (temperatura >= 40) {
                textView.setTextColor(textView.getResources().getColor(R.color.c_vermelho));
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Não foi possível decodificar " + nfe);
        }

    }

    public void implementaClick(Switch sw, final String rele){
        sw.setOnClickListener(
                new Switch.OnClickListener() {
                    public void onClick(View view) {
                       new HttpAsyncTask().execute(servidor+rele);
                    }
                });

    }

    public void implementaBotao(final Button bt, final String rele, final String mensagem, final int icone, final String[] estado, final boolean confirmar){
        bt.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View view) {
                        if (confirmar) {
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
                            // set title
                            alertDialogBuilder.setTitle("Confirmação");
                            alertDialogBuilder.setIcon(icone);
                            // set dialog message
                            alertDialogBuilder
                                    .setMessage(mensagem)
                                    .setCancelable(true)
                                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            if (rele.equals("acende externa")) {
                                                if (estado[4].equals("0"))
                                                    new HttpAsyncTask().execute(servidor+"?relay=1.");
                                                if (estado[5].equals("1"))
                                                    new HttpAsyncTask().execute(servidor+"?relay=2.");
                                            } else if (rele.equals("acende interna")) {
                                                for (int i = 6; i <= 7; i++) {
                                                    if (estado[i].equals("0"))
                                                        new HttpAsyncTask().execute(servidor+"?relay=" + (i - 3) + ".");
                                                }
                                                for (int i = 9; i <= 12; i++) {
                                                    if (estado[i].equals("0"))
                                                        new HttpAsyncTask().execute(servidor+"?relay=" + i  + ".");
                                                }
                                            } else
                                                new HttpAsyncTask().execute(servidor + rele);

                                        }
                                    })
                                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });
                            // create alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();
                            // show it
                            alertDialog.show();
                        } else {
                            // Sem confirmação
                            if (rele.equals("TV_SALA")) {
                                Button tv_sala = (Button) findViewById(R.id.bt_tvsala);
                                Button bt_lareira = (Button) findViewById(R.id.bt_lareira);
                                bt_lareira.setVisibility(Button.GONE);
                              //  LinearLayout botao2 = (LinearLayout) findViewById(R.id.lay_botao2);
                                if (findViewById(R.id.lay_tv).getVisibility() == View.GONE) {
                                    findViewById(R.id.lay_tv).setVisibility(View.VISIBLE);
                                    findViewById(R.id.lay_botao1).setVisibility(View.GONE);
                                //    botao2.setMinimumHeight(2);
                                    tv_sala.setText("Voltar");
                                } else {
                                    findViewById(R.id.lay_tv).setVisibility(View.GONE);
                                    findViewById(R.id.lay_botao1).setVisibility(View.VISIBLE);
                                //    botao2.setMinimumHeight(1);
                                    tv_sala.setText("TV\nSala");
                                    bt_lareira.setVisibility(Button.VISIBLE);
                                }
                            } else if (rele.equals("LR_SALA")) {
                                Button tv_sala = (Button) findViewById(R.id.bt_tvsala);
                                Button bt_lareira = (Button) findViewById(R.id.bt_lareira);
                                tv_sala.setVisibility(Button.GONE);
                                //  LinearLayout botao2 = (LinearLayout) findViewById(R.id.lay_botao2);
                                if (findViewById(R.id.lay_lareira).getVisibility() == View.GONE) {
                                    findViewById(R.id.lay_lareira).setVisibility(View.VISIBLE);
                                    findViewById(R.id.lay_botao1).setVisibility(View.GONE);
                                    //    botao2.setMinimumHeight(2);
                                    bt_lareira.setText("Voltar");
                                } else {
                                    findViewById(R.id.lay_lareira).setVisibility(View.GONE);
                                    findViewById(R.id.lay_botao1).setVisibility(View.VISIBLE);
                                    //    botao2.setMinimumHeight(1);
                                    bt_lareira.setText("Lareira\nSala");
                                    tv_sala.setVisibility(Button.VISIBLE);
                                }
                            } else new HttpAsyncTask().execute(servidor + rele);
                        }
                    }
                });

    }

    public class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        public String[] estado = {"0","0","0","0","0","0","0","0","0","0","0","0","0"};

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            /*
             *  Verifica se o retorno não é nulo e se tem o tamanho esperado.
             */

           // Toast.makeText(getBaseContext(), "(" + result.toString() + ") Conexão", Toast.LENGTH_LONG).show();
            if (result.matches("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")){

                servidor = "http://"+result.toString()+":8080/";
                new HttpAsyncTask().execute(servidor);
                Toast.makeText(getBaseContext(), "Novo IP Identificado:(" + servidor.toString() + ")", Toast.LENGTH_SHORT).show();
                // Salva valor localmente...
                SharedPreferences.Editor mEditor = mPrefs.edit();
                mEditor.putString("servidor", servidor.toString()).commit();
                procurandoServidor=0;
            } else
            if (result.contains("</DADOS>")) {
               // result = result +":19:25";

                result = result.substring(result.indexOf("<DADOS>")+7, result.indexOf("</DADOS>"));

                if (showConfig) Toast.makeText(getBaseContext(), "(" + result.toString() + ")", Toast.LENGTH_SHORT).show();

                // Verifica calendário da casa e executa o que estiver agendado.
                if (verAgenda) {
                    Toast.makeText(getBaseContext(), "( Consultando agendamentos... )", Toast.LENGTH_SHORT).show();

                    String selection = "((" +  CalendarContract.Events.CALENDAR_DISPLAY_NAME + " = ?) AND (" + CalendarContract.Events.DTSTART +" = ?))";
                    Calendar beginTime = Calendar.getInstance();
                    beginTime.set(2014, 7, 27);
                    long startMillis = beginTime.getTimeInMillis();

                    String[] selectionArgs = new String[] {"Casa da Lagoa", beginTime.getTime().toString()};

                    mCursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI, COLS, selection, selectionArgs, null);

                    mCursor.moveToFirst();
                    String title = "N/A";
                    Long start = 0L;
                    Format df = DateFormat.getDateFormat(getBaseContext());
                    Format tf = DateFormat.getTimeFormat(getBaseContext());

                    try {
                        /*
                        while (!mCursor.isLast()){
                            if (mCursor.getString(2).toString()!="Casa da Lagoa") {
                                mCursor.moveToNext();
                            } else break;
                        }
                        */
                        title = mCursor.getString(0);
                        start = mCursor.getLong(1);
                        Toast.makeText(getBaseContext(), "("+mCursor.getString(2).toString()+" - "+title.toString()+" "+df.format(start)+" at "+tf.format(start)+")", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                    //ignore
                    }

                }

                View botaoView = findViewById(R.id.lay_botao);
                View chaveView = findViewById(R.id.lay_chaves);
                View tempView = findViewById(R.id.lay_temp);

                estado = result.split(":");
                if (estado.length<21) {
                    Toast.makeText(getBaseContext(), "(" + result.toString() + ") Muito Curto", Toast.LENGTH_SHORT).show();
                }

                /*
                Estados
                1  -> Temperatura Circuitos
                2  -> Temperatura Externa
                3  -> Temperatura Interna
                4  -> Área de serviço
                5  -> Fundos
                6  -> Relê
                7  -> Relê
                8  -> Relê
                9  ->
                10 ->

                 */

                /*
                 *  Configura onClickListener dos botões de acordo com o status retornado
                 */

                if (botaoView!=null) {
                    ajustaBotoes(estado);
                    View lr_img_view = findViewById(R.id.lay_lr_img);

                    if (estado[21].contains("0")) {
                        lr_img_view.setBackgroundResource(R.drawable.bg_lareira_off);
                    } else {
                        lr_img_view.setBackgroundResource(R.drawable.bg_lareira);
                    }

                    TextView lr_temp = (TextView) findViewById(R.id.lr_temp); //  mViewPager.getChildAt(0).getRootView().
                    if (lr_temp != null) {
                        lr_temp.setText("Sala\n" + estado[3] + "º C");
                        ajustacor(lr_temp,  estado[3]);
                    }
                }

                /*
                    Ajusta as chaves de acordo com o retorno
                 */
                if (chaveView!=null) {
                    Switch chave2 = (Switch) findViewById(201); // 201 Área Serviço
                    if (estado[4].contains("1")) chave2.setChecked(true);
                    else chave2.setChecked(false);
                    implementaClick(chave2,"?relay=1.");

                    Switch chave1 = (Switch) findViewById(200); // 200 Fundos
                    if (estado[5].contains("0")) chave1.setChecked(true);
                    else chave1.setChecked(false);
                    implementaClick(chave1,"?relay=2.");

                    Switch chave3 = (Switch) findViewById(300); // 300 Quarto
                    implementaClick(chave3,"?relay=9.");
                    if (estado[9].contains("1")) chave3.setChecked(true);
                    else chave3.setChecked(false);

                    Switch chave4 = (Switch) findViewById(301); // 301 Closet
                    implementaClick(chave4,"?relay=10.");
                    if (estado[10].contains("1")) chave4.setChecked(true);
                    else chave4.setChecked(false);

                    Switch chave5 = (Switch) findViewById(302); // 302 Cab G
                    implementaClick(chave5,"?relay=11.");
                    if (estado[11].contains("1")) chave5.setChecked(true);
                    else chave5.setChecked(false);

                    Switch chave6 = (Switch) findViewById(303); // 303 Cab C
                    implementaClick(chave6,"?relay=12.");
                    if (estado[12].contains("1")) chave6.setChecked(true);
                    else chave6.setChecked(false);

                    Switch chave7 = (Switch) findViewById(304); // 300 Escritório
                    implementaClick(chave7,"?relay=3.");
                    if (estado[6].contains("1")) chave7.setChecked(true);
                    else chave7.setChecked(false);

                    Switch chave8 = (Switch) findViewById(305); // 301 Escada
                    implementaClick(chave8,"?relay=4.");
                    if (estado[7].contains("1")) chave8.setChecked(true);
                    else chave8.setChecked(false);
                }

                    /*
                    // Caso haja o fragment de temperaturas, preenche com o resultado
                     */

                if (tempView!=null) {

                        TextView tmpPlacas = (TextView) findViewById(401); //  mViewPager.getChildAt(0).getRootView().
                        if (tmpPlacas != null) {
                            tmpPlacas.setText("Circuitos\n" + estado[1] + "º C");
                            ajustacor(tmpPlacas, estado[1]);  //result.substring(17, 19)
                        }

                        TextView tmpExt = (TextView) findViewById(402); //  mViewPager.getChildAt(0).getRootView().
                        if (tmpExt != null) {
                            tmpExt.setText("Externa\n" + estado[2] + "º C");
                            ajustacor(tmpExt, estado[2]);
                        }

                        TextView tmpInt = (TextView) findViewById(403); //  mViewPager.getChildAt(0).getRootView().
                        if (tmpInt != null) {
                            tmpInt.setText("Casa\n" + estado[3] + "º C");
                            ajustacor(tmpInt,  estado[3]);
                        }
                        /*
                        WebView tela = (WebView) findViewById(1001);

                        if ((tela!=null)&&(tela.getVisibility()==View.VISIBLE)){
                            tela.loadUrl(servidorGrafico);
                        }
                        */
                }

                /*
                for (int i=1;i<estado.length;i++) {
                    Toast.makeText(getBaseContext(), "(" + estado[i] + ") Estado "+ i, Toast.LENGTH_SHORT).show();
                }
                 */

            } else {
                Toast.makeText(getBaseContext(), "Resultado Não Esperado: (" + result.toString() + ") Buscando novo servidor...", Toast.LENGTH_LONG).show();
                if (procurandoServidor<=3) {
                    new HttpAsyncTask().execute(strPlanilha);
                    procurandoServidor++;
                }
            }
        }
    }
}
