package hel.def.wisp.deffie_hellmanwhisper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import hel.def.wisp.deffie_hellmanwhisper.fragments.ChatFragment;

public class MainActivity extends FragmentActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = new ChatFragment();
        transaction.replace(R.id.main_frame, fragment, ChatFragment.TAG);
        transaction.addToBackStack(ChatFragment.TAG);
        transaction.commit();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.toolbar_menu);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.chat:
                        FragmentTransaction transaction;
                        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ChatFragment.TAG);
                        if (fragment == null || !fragment.isAdded()) {
                            transaction = getSupportFragmentManager().beginTransaction();
                            transaction.replace(R.id.main_frame, new ChatFragment(), ChatFragment.TAG);
                            transaction.addToBackStack(ChatFragment.TAG);
                            transaction.commit();
                        }
                        break;
                    case R.id.settings:
                        Toast.makeText(MainActivity.this, "Open settig", Toast.LENGTH_SHORT).show();
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//            if (id == R.id.action_settings) {
//                return true;
//            }

        return super.onOptionsItemSelected(item);
    }

}
