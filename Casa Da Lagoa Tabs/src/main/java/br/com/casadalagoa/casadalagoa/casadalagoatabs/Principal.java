package br.com.casadalagoa.casadalagoa.casadalagoatabs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Locale;

import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
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

    public final HttpAsyncTask mHttpAsyncTask = new HttpAsyncTask();

    public void enviaGet(String rele){

        mHttpAsyncTask.execute("http://georgesilva.dyndns.org:8080/?"+rele);
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


        // Verifica se há conexão e faz solicitação inicial
        if(isConnected()){
            //Toast.makeText(getBaseContext(), "Conectado! Solicitando dados iniciais...", Toast.LENGTH_SHORT).show();
            //new HttpAsyncTask().execute("http://georgesilva.dyndns.org:8080/");
          //  GET("http://georgesilva.dyndns.org:8080/"); // Não funciona, retorna null
        }
        else{
            Toast.makeText(getBaseContext(), "Não Conectado!", Toast.LENGTH_LONG).show();
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
            case R.id.menu_internal: actionBar.setSelectedNavigationItem(2);  break;
            case R.id.menu_external: actionBar.setSelectedNavigationItem(1);  break;
            case R.id.menu_temp: actionBar.setSelectedNavigationItem(3);  break;
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
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 3:
                    return getString(R.string.title_section4).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 0:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
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
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public void onClick(View v){
            switch(v.getId()){
                case R.id.button:

                    break;

                // Just like you were doing
            }
        }
        public PlaceholderFragment() {
        }

        public void criaChave(View rootView, String[] conteudo){
            LinearLayout chaves = (LinearLayout) rootView.findViewById(R.id.lay_chaves);


            for (int i=0;i<conteudo.length ;i++) {
                Switch chave_nova = new Switch(this.getActivity());
                chave_nova.setText(conteudo[i]);
                chave_nova.setLayoutParams(rootView.findViewById(R.id.switch1).getLayoutParams());
                chave_nova.setId(i + (100 * getArguments().getInt(ARG_SECTION_NUMBER)));
                chave_nova.setTextOff("Delisgado");
                chave_nova.setTextOn("Ligado");
                chave_nova.setBackgroundColor(getResources().getColor(R.color.black_overlay));
                chave_nova.setTextAppearance(chave_nova.getContext(),R.style.ChaveTextAppearance);
                chave_nova.setSwitchTextAppearance(chave_nova.getContext(),R.style.SwitchTextAppearance);
                if (conteudo[i]=="Reservado") {
                    chave_nova.setActivated(false);
                    chave_nova.setEnabled(false);
                } else {
                    chave_nova.setOnClickListener( new  View.OnClickListener() {
                        public void onClick(View view) {
                            String rele="";
                            switch(view.getId()){

                                // Chaves Internas
                                case 200:
                                     rele = "1";
                                    break;
                                case 201:
                                     rele = "2";
                                    break;
                                case 202:
                                    rele = "3";
                                    break;
                                case 203:
                                    rele = "4";
                                    break;
                                case 204:
                                    rele = "10";
                                    break;
                                case 205:
                                    rele = "11";
                                    break;

                                // Chaves Internas
                                case 300:
                                    rele = "6";
                                    break;
                                case 301:
                                    rele = "7";
                                    break;
                                case 302:
                                    rele = "8";
                                    break;
                                case 303:
                                    rele = "12";
                                    break;
                                case 304:
                                    rele = "13";
                                    break;
                                case 305:
                                    rele = "14";
                                    break;
                            }

                            Toast.makeText(view.getContext(), "(" + rele + ") Acionar"+view.getId(), Toast.LENGTH_SHORT).show();


                        }
                    });
                }
                chaves.addView(chave_nova);
            }
        }

        // Declara chaves já disponíveis
        public final String[] arr_Interior = { "Escritório", "Escada", "Reservado", "Reservado", "Reservado" };
        public final String[] arr_Exterior = { "Fundos", "Área de\nServiço", "Reservado", "Reservado", "Reservado" };




        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            View botaoView = rootView.findViewById(R.id.lay_botao);
            View chaveView = rootView.findViewById(R.id.lay_chaves);
            View tempView = rootView.findViewById(R.id.lay_temp);

          //  TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            //textView.setText(this.getPageTitle())
            //textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));

            /*

            Ajusta as telas...

             */

            switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
                case 4:// Temperatura
                    botaoView.setVisibility(View.GONE);
                    chaveView.setVisibility(View.GONE);
                    tempView.setVisibility(View.VISIBLE);
                    TextView tempPlaca = (TextView) tempView.findViewById(R.id.tmpPlaca);
                    tempPlaca.setId(401);
                    TextView tempExt = (TextView) tempView.findViewById(R.id.tmpExt);
                    tempExt.setId(402);
                    TextView tempInt = (TextView) tempView.findViewById(R.id.tmpInt);
                    tempInt.setId(403);
                    /*
                    TextView tempInterna = (TextView) tempView.findViewById(R.id.tmpPlaca);
                    temp.setId(401);
                    TextView tempPlaca = (TextView) tempView.findViewById(R.id.tmpPlaca);
                    tempPlaca.setId(401);
                    */
                    break; //  textView.setTag(getArguments().getInt(ARG_SECTION_NUMBER), tempObj);
                case 2: // Exterior
                    botaoView.setVisibility(View.GONE);
                    chaveView.setVisibility(View.VISIBLE);
                    tempView.setVisibility(View.GONE);
                    criaChave(rootView, arr_Exterior);
                    break;
                case 3: // Interior
                    botaoView.setVisibility(View.GONE);
                    tempView.setVisibility(View.GONE);
                    chaveView.setVisibility(View.VISIBLE);
                    criaChave(rootView, arr_Interior);
                    break;
                case 1: //Dispositivos
                    chaveView.setVisibility(View.GONE);
                    tempView.setVisibility(View.GONE);
                    botaoView.setVisibility(View.VISIBLE);
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
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Não funcionou";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
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

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }

        public void ajustacor( TextView textView, String retorno){
            try {
                int temperatura = 0;
                temperatura = Integer.parseInt(retorno);
                if (temperatura < 20)
                    textView.setTextColor(getResources().getColor(R.color.c_azul));
                else if ((temperatura >= 20) && (temperatura < 30))
                    textView.setTextColor(getResources().getColor(R.color.c_verde));
                else if ((temperatura >= 30) && (temperatura < 40))
                    textView.setTextColor(getResources().getColor(R.color.c_laranja));
                else if (temperatura >= 40) {
                    textView.setTextColor(getResources().getColor(R.color.c_vermelho));
                }
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

        }

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            //ActionBar actionBar = getSupportActionBar();
            //actionBar.setSelectedNavigationItem(3);
            if (!result.equals("")){

                View chaveView = findViewById(R.id.lay_chaves);
                View tempView = findViewById(R.id.lay_temp);

                /*
                    Ajusta as chaves de acordo com o retorno
                 */
                if (chaveView!=null) {
                    Switch chave2 = (Switch) findViewById(201); // 201 Área
                    if (result.substring(1, 2).contains("1")) chave2.setChecked(true);
                    else chave2.setChecked(false);

                    Switch chave1 = (Switch) findViewById(200); // 200 Fundos, 201 Área
                    if (result.substring(3, 4).contains("0")) chave1.setChecked(true);
                    else chave1.setChecked(false);

                    Switch chave3 = (Switch) findViewById(300); // 300 Escritório
                    if (result.substring(5, 6).contains("1")) chave3.setChecked(true);
                    else chave3.setChecked(false);

                    Switch chave4 = (Switch) findViewById(301); // 301 Escada
                    if (result.substring(7, 8).contains("1")) chave4.setChecked(true);
                    else chave4.setChecked(false);
                }


                    /*
                    // Caso haja o fragment de temperaturas, preenche com o resultado
                     */
                if (tempView!=null) {

                        TextView tmpPlacas = (TextView) findViewById(401); //  mViewPager.getChildAt(0).getRootView().
                        if (tmpPlacas != null) {
                            tmpPlacas.setText("Circuitos\n" + result.substring(17, 19) + "ºC");
                            ajustacor(tmpPlacas, result.substring(17, 19));
                        }

                        TextView tmpExt = (TextView) findViewById(402); //  mViewPager.getChildAt(0).getRootView().
                        if (tmpExt != null) {
                            tmpExt.setText("Externa\n" + result.substring(17, 19) + "ºC");
                            ajustacor(tmpExt,  result.substring(17, 19));
                        }

                        TextView tmpInt = (TextView) findViewById(403); //  mViewPager.getChildAt(0).getRootView().
                        if (tmpInt != null) {
                            tmpInt.setText("Interna\n" + result.substring(17, 19) + "ºC");
                            ajustacor(tmpInt,  result.substring(17, 19));
                        }

                }


                //TextView textView = (TextView) chaveView.findViewById(R.id.section_label);
                //mSectionsPagerAdapter.getItem(0).getView().toString();
                //mViewPager.getChildAt(1).findViewById(R.id.section_label); //chaveView.findViewById(R.id.section_label);
                //textView.setText("(" + result + ") " + "(" + ") " + mViewPager.getCurrentItem() + " " + " - " + mSectionsPagerAdapter.getPageTitle(mViewPager.getCurrentItem())+"("+result.substring(17,19));

               // Toast.makeText(getBaseContext(), "(" + result.substring(17, 19) + ") " + mSectionsPagerAdapter.getPageTitle(mViewPager.getCurrentItem()), Toast.LENGTH_LONG).show();

            } else Toast.makeText(getBaseContext(), "(" + result + ") Sem Conexão", Toast.LENGTH_LONG).show();
        }
    }
}
