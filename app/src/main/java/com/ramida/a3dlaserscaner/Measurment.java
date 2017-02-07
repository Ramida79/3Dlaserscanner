package com.ramida.a3dlaserscaner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by Ramida on 2017-01-13.
 */

public class Measurment  extends Activity {

    private ListView mainListView ;
    private TextView tInfoMeasurments;
    private Button bAnalize;
    private  Button bSTLView;
    private ImageView imView;


    private Context myContext;
    private int actualImageView,selectedMeasurment;

    private String fileFolder = "/sdcard/3DScannerData/";

    private ArrayAdapter<String> listAdapter ;



    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.measurment);
        myContext= this;
        actualImageView=0;

        mainListView = (ListView) findViewById( R.id.listFolders );
        tInfoMeasurments = (TextView) findViewById(R.id.textInfoMeasurments);
        bAnalize = (Button) findViewById(R.id.bStartImagesAnalize);
        imView =(ImageView) findViewById(R.id.imageView);
        bSTLView = (Button) findViewById(R.id.bVIEWstl);
        bSTLView.setEnabled(false);


        bSTLView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {

                                            Intent myIntent = new Intent(myContext, STLViewActivity.class);

                                            myContext.startActivity(myIntent);
                                        }


                                    }
        );

        imView.setOnClickListener(new View.OnClickListener() {
                                      @Override
                                      public void onClick(View v) {

                                          if (selectedMeasurment>=0) {
                                              actualImageView++;

                                              File dir = new File(fileFolder + mainListView.getItemAtPosition(selectedMeasurment).toString());

                                              File[] filelist = dir.listFiles();

                                              if (actualImageView>=filelist.length) actualImageView=0;


                                              Bitmap bmp = BitmapFactory.decodeFile(filelist[actualImageView].getAbsolutePath());
                                              imView.setImageBitmap(bmp);
                                              Toast.makeText(getApplicationContext(), filelist[actualImageView].getAbsolutePath().toString(), Toast.LENGTH_SHORT)
                                                      .show();


                                          }
                                      }
                                  }
        );

        File dir = new File("/sdcard/3DScannerData/");
        File[] filelist = dir.listFiles();
        String[] theNamesOfFiles = new String[filelist.length];
        for (int i = 0; i < theNamesOfFiles.length; i++) {
            theNamesOfFiles[i] = filelist[i].getName();
        }

        listAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, theNamesOfFiles);
        mainListView.setAdapter( listAdapter );


        mainListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
                String kat="brak";
                String KatMax="brak";
                String existSTL="brak";
                selectedMeasurment=position;
                //view.setSelected(true);
                for (int j = 0; j < parent.getChildCount(); j++)
                    parent.getChildAt(j).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));

                // change the background color of the selected element
                view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

                File dir = new File(fileFolder + mainListView.getItemAtPosition(position).toString());

                File[] filelist = dir.listFiles();

                bSTLView.setEnabled(false);

                String[] theNamesOfFiles = new String[filelist.length];
                for (int i = 0; i < theNamesOfFiles.length; i++) {
                    theNamesOfFiles[i] = filelist[i].getName();

                    if(filelist[i].getName().contains(".stl"))
                    {
                        existSTL="JEST STL";
                        bSTLView.setEnabled(true);

                    }
                }



                if(theNamesOfFiles.length>2) {
                    kat="";
                    KatMax="";
                    boolean podkresl, kropka;
                    int roznica=0;
                    podkresl= kropka= false;
                    int a=0;
                    while(!kropka)
                    {
                        if(theNamesOfFiles[1].charAt(a)=='_')
                        {
                            podkresl=true;
                            a++;
                        }
                        if(theNamesOfFiles[1].charAt(a)=='.')
                        {
                            kropka=true;
                        }

                        if(podkresl && !kropka)       kat += theNamesOfFiles[1].charAt(a);

                        a++;
                    }

                    podkresl= kropka= false;
                    a=0;
                    while(!kropka)
                    {
                        if(theNamesOfFiles[(theNamesOfFiles.length)-1].charAt(a)=='_')
                        {
                            podkresl=true;
                            a++;
                        }
                        if(theNamesOfFiles[(theNamesOfFiles.length)-1].charAt(a)=='.')
                        {
                            kropka=true;
                        }

                        if(podkresl && !kropka)       KatMax += theNamesOfFiles[(theNamesOfFiles.length)-1].charAt(a);

                        a++;
                    }



                }

                tInfoMeasurments.setText("Ilosc plikow w folderze: "+theNamesOfFiles.length+ "\nObrot o kat:"+ kat+"\nCalkowity kat pomiarów: "+KatMax+"\n JEST stl czy nie? " + existSTL);
                //URL url = new URL("http://image10.bizrate-images.com/resize?sq=60&uid=2216744464");
                if(filelist.length>0) {
                    Bitmap bmp = BitmapFactory.decodeFile(filelist[0].getAbsolutePath());
                    imView.setImageBitmap(bmp);
                }
                else
                    imView.setImageResource(android.R.color.transparent);

            }
        });


    }




    @Override
    public void finish() {

           super.finish();
    }




    private class ImageProcessing extends  Thread{

        String FileName;

        public ImageProcessing(String file)
        {
            FileName = file ;//new String(file);
        }

        public void run()
        {


        }
    }



}
