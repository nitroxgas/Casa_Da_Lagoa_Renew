package br.com.casadalagoa.casadalagoa.casadalagoatabs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.preference.SwitchPreference;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;


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

    public  HttpAsyncTask mHttpAsyncTask = new HttpAsyncTask();

    @Override
    protected void onResume(){
        super.onResume();
        // Verifica se há conexão e faz solicitação inicial
        if(isConnected()){

        }
        else{
            Toast.makeText(getBaseContext(), "Não Conectado!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_principal);

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
        switch(item.getItemId()) {
            case R.id.menu_toggle_log: break;
            case R.id.menu_atualizar: new HttpAsyncTask().execute("http://georgesilva.dyndns.org:8080/"); break;
            case R.id.menu_devices: actionBar.setSelectedNavigationItem(0);  break;
            //case R.id.menu_internal: actionBar.setSelectedNavigationItem(2);  break;
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
        new HttpAsyncTask().execute("http://georgesilva.dyndns.org:8080/");
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
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
                case 3:
                    return getString(R.string.title_section2).toUpperCase(l);
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
                chave_nova.setTextOff("Delisgado");
                chave_nova.setTextOn("Ligado");
                chave_nova.setBackgroundColor(getResources().getColor(R.color.black_overlay));
                chave_nova.setTextAppearance(chave_nova.getContext(),R.style.ChaveTextAppearance);
                chave_nova.setSwitchTextAppearance(chave_nova.getContext(),R.style.SwitchTextAppearance);
                if (conteudo[i]=="Reservado") {
                    chave_nova.setActivated(false);
                    chave_nova.setEnabled(false);
                }
                chaves.addView(chave_nova);
            }
        }

        // Declara chaves já disponíveis
        public final String[] arr_Interior = { "Escritório", "Escada", "Reservado", "Reservado"  };
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
                    TextView tempInt = (TextView) tempView.findViewById(R.id.tmpInt);
                    tempInt.setId(403);
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
            return rootView;
        }
    }

    //
    // Sessão para tratar das comunicações:
    //
    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

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

            temperatura = Integer.parseInt(retorno.substring(0,retorno.indexOf(".")));



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
            System.out.println("Could not parse " + nfe);
        }

    }

    public void implementaClick(Switch sw, final String rele){
        sw.setOnClickListener(
                new Switch.OnClickListener() {
                    public void onClick(View view) {
                       new HttpAsyncTask().execute("http://georgesilva.dyndns.org:8080/"+rele);
                    }
                });

    }




    public void implementaBotao(final Button bt, final String rele, final String mensagem, final int icone, final String[] estado){

        bt.setOnClickListener(
                new Button.OnClickListener() {
                    public void onClick(View view) {
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
                                                new HttpAsyncTask().execute("http://georgesilva.dyndns.org:8080/?relay=1.");

                                            if (estado[5].equals("1"))
                                                new HttpAsyncTask().execute("http://georgesilva.dyndns.org:8080/?relay=2.");

                                        } else if (rele.equals("acende interna")) {
                                            for (int i=6; i<=7; i++) {
                                                if (estado[i].equals("0"))
                                                    new HttpAsyncTask().execute("http://georgesilva.dyndns.org:8080/?relay=" + (i-3) + ".");
                                            }
                                        } else
                                            new HttpAsyncTask().execute("http://georgesilva.dyndns.org:8080/" + rele); //http://georgesilva.dyndns.org:8080/" + rele);
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
                    }
                });

    }

    public class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            /*
             *  Verifica se o retorno não é nulo e se tem o tamanho esperado.
             */

            if (result.contains("</DADOS>")){
               // result = result +":19:25";

                result = result.substring(result.indexOf("<DADOS>")+7, result.indexOf("</DADOS>"));

                //Toast.makeText(getBaseContext(), "(" + result.toString() + ")", Toast.LENGTH_LONG).show();

                final String[] estado = result.split(":");

                View botaoView = findViewById(R.id.lay_botao);
                View chaveView = findViewById(R.id.lay_chaves);
                View tempView = findViewById(R.id.lay_temp);

                /*
                    Ajusta as chaves de acordo com o retorno
                 */

                if (botaoView!=null){
                    Button portao = (Button) findViewById(100);
                    implementaBotao(portao, "?relay=50.", "Abrir o portão ?",R.drawable.ic_remote_g, estado);
                    Button alimentador = (Button) findViewById(101);
                    implementaBotao(alimentador, "?relay=RACAO",  "Acionar o alimentador ?",R.drawable.ic_dog_g, estado);
                    Button internet = (Button) findViewById(102);
                    implementaBotao(internet, "?relay=INTERNET", "Reiniciar a internet ?",R.drawable.abc_ic_go, estado);
                    Button tv = (Button) findViewById(R.id.bt_tvsala);
                    implementaBotao(tv, "?relay=TVSALA", "Ligar/Desligar TV ?",R.drawable.ic_remote, estado);
                    Button lig_todas = (Button) findViewById(R.id.bt_ligatudo);
                    implementaBotao(lig_todas, "acende externa", "Ligar Luzes Externas ?",R.drawable.ic_remote, estado);
                    Button lig_internas = (Button) findViewById(R.id.bt_liga_internas);
                    implementaBotao(lig_internas, "acende interna", "Ligar Luzes Internas ?",R.drawable.ic_remote, estado);
                }

                if (chaveView!=null) {
                    Switch chave2 = (Switch) findViewById(201); // 201 Área
                    if (estado[4].contains("1")) chave2.setChecked(true);
                    else chave2.setChecked(false);
                    implementaClick(chave2,"?relay=1.");

                    Switch chave1 = (Switch) findViewById(200); // 200 Fundos, 201 Área
                    if (estado[5].contains("0")) chave1.setChecked(true);
                    else chave1.setChecked(false);
                    implementaClick(chave1,"?relay=2.");

                    Switch chave3 = (Switch) findViewById(300); // 300 Escritório
                    implementaClick(chave3,"?relay=3.");
                    if (estado[6].contains("1")) chave3.setChecked(true);
                    else chave3.setChecked(false);

                    Switch chave4 = (Switch) findViewById(301); // 301 Escada
                    implementaClick(chave4,"?relay=4.");
                    if (estado[7].contains("1")) chave4.setChecked(true);
                    else chave4.setChecked(false);
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
                            tmpInt.setText("Dentro de\nCasa\n" + estado[3] + "º C");
                            ajustacor(tmpInt,  estado[3]);
                        }

                }

                /*
                for (int i=1;i<estado.length;i++) {
                    Toast.makeText(getBaseContext(), "(" + estado[i] + ") Estado "+ i, Toast.LENGTH_SHORT).show();
                }
                 */

            } else Toast.makeText(getBaseContext(), "(" + result.toString() + ") Conexão", Toast.LENGTH_LONG).show();
        }
    }
}
