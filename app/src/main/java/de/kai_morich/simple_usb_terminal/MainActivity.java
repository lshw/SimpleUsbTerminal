package de.kai_morich.simple_usb_terminal;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.widget.Toast;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (savedInstanceState == null)
            getSupportFragmentManager().beginTransaction().add(R.id.fragment, new DevicesFragment(), "devices").commit();
        else
            onBackStackChanged();
    }

    @Override
    public void onBackStackChanged() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(getSupportFragmentManager().getBackStackEntryCount()>0);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if ("android.hardware.usb.action.USB_DEVICE_ATTACHED".equals(intent.getAction())) {
            TerminalFragment terminal = (TerminalFragment)getSupportFragmentManager().findFragmentByTag("terminal");
            if (terminal != null)
                terminal.status("USB device detected");
        }
        super.onNewIntent(intent);
    }

    void showAboutDialog() {
        String appName = getString(R.string.app_name);
        String version = getString(R.string.version_label, BuildConfig.VERSION_NAME);
        String deviceDesc = getString(R.string.about_device_desc);
        String message = getString(R.string.about_message, appName, version, deviceDesc);
        new AlertDialog.Builder(this)
                .setTitle(R.string.about)
                .setMessage(message)
                .setPositiveButton(R.string.about_web, (dialog, which) -> openUrl(R.string.about_website_url))
                .setNeutralButton(R.string.about_product, (dialog, which) -> openUrl(R.string.about_product_url))
                .setNegativeButton(android.R.string.ok, null)
                .show();
    }

    private void openUrl(int urlResId) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(urlResId)));
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.about_open_web_failed, Toast.LENGTH_SHORT).show();
        }
    }

    void openLogsDirectory() {
        if (!LogFiles.ensureLogsDirectoryExists(this)) {
            Toast.makeText(this, R.string.logs_create_failed, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!LogFiles.usesPublicLogs()) {
            Toast.makeText(this, R.string.logs_open_unsupported, Toast.LENGTH_SHORT).show();
            return;
        }
        Uri documentUri = LogFiles.getLogsDirectoryUri();
        Uri treeUri = LogFiles.getLogsTreeUri();
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(documentUri, DocumentsContract.Document.MIME_TYPE_DIR)
                .putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentUri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        try {
            startActivity(intent);
        } catch (Exception viewException) {
            try {
                startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                Toast.makeText(this, getString(R.string.logs_open_fallback, LogFiles.getPublicLogsDisplayPath()), Toast.LENGTH_LONG).show();
            } catch (Exception downloadsException) {
                Toast.makeText(this, R.string.logs_open_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

}
