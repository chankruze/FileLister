package in.geekofia.filelister;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import in.geekofia.filelister.adapters.FileListAdapter;
import in.geekofia.filelister.models.Item;

public class MainActivity extends ListActivity {

    private ListView listView;
    private File currentDir;
    private String rootDirPath;
    private FileListAdapter adapter;
    private ImageView imageView;
    private  TextView textView;

    private int STORAGE_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(android.R.id.list);
        imageView = findViewById(R.id.error_logo);
        textView = findViewById(R.id.error_desc);

        //permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "You have alredy granted this permission !", Toast.LENGTH_SHORT).show();
            loadFiles();
        } else {
            requestStoragePermission();
        }
    }

    private void loadFiles() {
        currentDir = Environment.getExternalStorageDirectory();
        rootDirPath = currentDir.getName();
        fillDirectory(currentDir);

        listView.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.GONE);
        textView.setVisibility(View.GONE);
    }

    private void requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){

            new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("This permission is required to read from external storage")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create()
                    .show();

        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
                loadFiles();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                listView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                textView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void fillDirectory(File file){
        setTitle(file.getName());

        //Retrieve all files in this directory
        File[] dirs = file.listFiles();

        // list of directories
        List<Item> directories = new ArrayList<Item>();

        // list of files
        List<Item> files = new ArrayList<Item>();

        for(File f : dirs){
            // is a directory
            if(f.isDirectory()){
                File[] innerFiles = f.listFiles();
                int numItems;
                if(innerFiles!=null)
                    numItems = innerFiles.length;
                else
                    numItems = 0;

                Item item = new Item(Item.DIRECTORY, f.getName(), numItems, f.getAbsolutePath());
                directories.add(item);
            }
            // is a file
            else{
                Item item = new Item(Item.FILE, f.getName(), f.length(), f.getAbsolutePath());
                files.add(item);
            }
        }

        directories.addAll(files);

        if(!currentDir.getName().equals(rootDirPath)){
            directories.add(0, new Item(Item.UP, "", file.getParent()));
        }

        adapter = new FileListAdapter(this, directories);
        listView.setAdapter(adapter);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Item item = (Item) adapter.getItem(position);

        int typeItem = item.getTypeItem();
        if(typeItem==Item.DIRECTORY || typeItem==Item.UP){
            currentDir = new File(item.getAbsolutePath());
            fillDirectory(currentDir);
        }
        else{
            chooseFile(item);
        }
    }

    public void chooseFile(final Item item){
        AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
        newDialog.setTitle("Send file");
        newDialog.setMessage("Are you sure you want to choose this file ?");

        newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = getIntent();
                intent.putExtra("filePath", item.getAbsolutePath());
                setResult(RESULT_OK, intent);
                finish();
            }

        });

        newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        newDialog.show();
    }

    @Override
    public void onBackPressed() {
        if(!currentDir.getName().equals(rootDirPath)){
            fillDirectory(currentDir.getParentFile());
            currentDir = currentDir.getParentFile();
        }
        else{
            finish();
        }
    }
}
